package com.cashmallow.api.auth.impl;

import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.auth.UserAuth;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.user.*;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.common.CustomStringUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.security.Key;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 기능: token 사용자의 login 여부 및 권한 검사를 위한 class
 *
 * @author swshin
 */
@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final int EXPIRATION_REFRESH_TOKEN = 1440; // 24 hours * 60 minutes
    private static final int EXPIRATION_ACCESS_TOKEN = 10; // 1 hours * 60 minutes

    private static final int EXPIRATION_MINUTES = 10; // 2018-05-23 reduce expiration time (12 hours --> 10 minutes)
    private static final int EXPIRATION_MINUTES_ADMIN = 30; // 2018-07-09 reduce expiration time (10 minutes --> 30
    // minutes)
    private static final int UPDATE_MINUTES = 1; // 2018-05-23 DB의 최종 접속 시간 업데이트 주기

    private static int sync = 0;

    private String lastJsonWebTokenId;

    @Value("${json.web.token.key}")
    private String jsonWebTokenKey;

    @Autowired
    SecurityService securityService;

    @Autowired
    private RefreshTokenMapper refreshTokenMapper;

    @Autowired
    private AccessTokenMapper accessTokenMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * generate Json Web Token key
     *
     * @return
     */
    private Key generateKey() {
        return Keys.hmacShaKeyFor(jsonWebTokenKey.getBytes());
    }

    @Override
    public Jws<Claims> parseJWT(String token) {

        if (token.startsWith(TOKEN_PREFIX)) {
            token = token.replaceFirst(TOKEN_PREFIX, "").trim();
        }

        return Jwts.parser().setSigningKey(generateKey()).parseClaimsJws(token);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.cashmallow.api.auth.AuthService#issueRefreshToken(java.lang.String,
     * java.lang.Long, java.lang.String)
     */
    @Override
    @Transactional(rollbackFor = CashmallowException.class)
    public String issueRefreshToken(String login, Long userId, String auths) throws CashmallowException {
        String token = "";
        RefreshToken refreshToken = new RefreshToken();

        if (!StringUtils.isEmpty(login) && userId > 0 && !StringUtils.isEmpty(auths)) {

            refreshToken.setLogin(login);
            refreshToken.setRefreshTime(Timestamp.valueOf(LocalDateTime.now()));
            refreshToken.setUserId(userId);
            refreshToken.setAuths(auths);

            User user = userMapper.getUserByUserId(userId);
            refreshToken.setServiceCountry(CountryCode.of(user.getCountry()).name());

            if (user != null && !StringUtils.isEmpty(user.getInstanceId())) {
                refreshToken.setInstanceId(user.getInstanceId());
            }

            // token을 생성한다.
            token = generateJsonWebToken(refreshToken, EXPIRATION_REFRESH_TOKEN);

            // Set token in RefreshToken object.
            String encodedToken = securityService.encryptSHA2(token);
            refreshToken.setToken(encodedToken);

            refreshTokenMapper.insertRefreshToken(refreshToken);

        }

        logger.info("issueRefreshToken(): user_id={}, auths={}", refreshToken.getUserId(), refreshToken.getAuths());

        return token;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.cashmallow.api.auth.AuthService#issueAccessToken(java.lang.String)
     */
    @Override
    public String issueAccessToken(String refreshToken) {
        String accessToken = "";

        try {

            Jws<Claims> jws = parseJWT(refreshToken);

            Claims body = jws.getBody();
            String userId = body.get("user_id").toString();

            if (StringUtils.isEmpty(userId)) {
                return null;
            }

            Date now = new Date();
            Date expriration = body.getExpiration();

            if (now.after(expriration)) {
                return null;
            }

            logger.info("issueAccessToken(): user_id={}, now={}, expriration={}", userId, now, expriration);

        } catch (JwtException e) {
            logger.error(e.getMessage(), e);
            return null;
        }

        String encodedToken = securityService.encryptSHA2(refreshToken);
        RefreshToken t = refreshTokenMapper.getRefreshTokenByToken(encodedToken);

        if (t != null) {
            try {
                accessToken = generateJsonWebToken(t, EXPIRATION_ACCESS_TOKEN);
            } catch (CashmallowException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return accessToken;
    }

    /**
     * generate Json Web Token id. yyyyMMdd.[count]. The count is refreshed every
     * day or when the server restarts.
     *
     * @return
     */
    private String getJsonWebTokenId() {
        String id = "";
        long seq = 1L;

        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        String date = df.format(new Date());

        if (!StringUtils.isEmpty(lastJsonWebTokenId)) {

            String[] s = lastJsonWebTokenId.split("\\.");

            if (s[0].equals(date)) {
                seq = Long.parseLong(s[1]) + 1;
                id = String.format("%s.%d", date, seq);
            }
        }

        if (StringUtils.isEmpty(id)) {
            id = String.format("%s.%d", date, seq);
        }

        lastJsonWebTokenId = id;

        logger.info("getJsonWebTokenId(): id={}", id);

        return id;
    }

    /**
     * generate Json Web Token
     *
     * @param refreshToken
     * @param timeout      Token timeout value. EXPIRATION_REFRESH_TOKEN or
     *                     EXPIRATION_ACCESS_TOKEN
     * @return generated token
     * @throws CashmallowException
     */
    private String generateJsonWebToken(RefreshToken refreshToken, int timeout) throws CashmallowException {
        String token = null;

        if (refreshToken == null) {
            throw new CashmallowException(Const.MSG_INVALID_TOKEN);
        } else {

            String id = getJsonWebTokenId();

            Date issueAt = new Date();

            Calendar cal = Calendar.getInstance();
            cal.setTime(issueAt);
            cal.add(Calendar.MINUTE, timeout);
            Date expiration = cal.getTime();

            String userId = refreshToken.getUserId().toString();
            String role = refreshToken.getAuths();
            String instanceId = refreshToken.getInstanceId();

            token = Jwts.builder()
                    .setId(id)
                    .setIssuedAt(issueAt)
                    .setExpiration(expiration)
                    .claim("user_id", userId)
                    .claim("role", role)
                    .claim("instanceId", instanceId)
                    .claim("serviceCountry", refreshToken.getServiceCountry())
                    .signWith(generateKey()).compact();

            logger.info("generateJsonWebToken(): id={}, userId={}, role={}, issueAt={}, expiration={}", id, userId,
                    role, issueAt, expiration);

        }

        return token;
    }

    @Override
    public UserAuth getUserInfo(String token) {
        return getUserInfo(token, false);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.cashmallow.api.auth.AuthService#getUserInfo(java.lang.String,
     * boolean)
     */
    @Override
    public UserAuth getUserInfo(String token, boolean isRenewal) {
        String method = "getUserInfo(): ";

        if (StringUtils.isEmpty(token) || isHexaStr(token)) {
            return null;
        }

        // New token (Json Web Token)
        try {

            Jws<Claims> jws = parseJWT(token);

            Claims body = jws.getBody();
            String userId = body.get("user_id").toString();
            String role = body.get("role").toString();
            String instanceId = body.get("instanceId").toString();
            String serviceCountry = body.get("serviceCountry").toString();

            Date now = new Date();

            logger.debug("{}: Token expirationMinutes={}, issutAt={}, expiration={}, now={}", method,
                    EXPIRATION_ACCESS_TOKEN, body.getIssuedAt(), body.getExpiration(), now);

            if (now.after(body.getExpiration())) {
                logger.info("{}: Token expired. expirationMinutes={}, issutAt={}, expiration={}, now={}", method,
                        EXPIRATION_ACCESS_TOKEN, body.getIssuedAt(), body.getExpiration(), now);
                return null;
            }

            List<String> auths = new ArrayList<>();
            for (Object o : new JSONArray(role).toList()) {
                auths.add(o.toString());
            }

            return new UserAuth(Long.parseLong(userId), instanceId, auths, serviceCountry);
        } catch (ExpiredJwtException e) {
            logger.warn(e.getMessage(), e);
            logger.warn("{}: Not Json Web Token Expired", method);
        } catch (JwtException e) {
            logger.warn(e.getMessage(), e);
            logger.warn("{}: Not Json Web Token", method);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.cashmallow.api.auth.AuthService#getUserAuthInfo(java.lang.String)
     */
    @Override
    public List<String> getUserAuthInfo(String token) {
        // 권한 체크를 위해 UserInfo 조회 시 token 갱신 안함.
        UserAuth userAuth = getUserInfo(token, false);

        if (userAuth != null) {
            return userAuth.getAuths();
        }

        return List.of();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.cashmallow.api.auth.AuthService#containsRole(java.lang.String,
     * java.lang.String)
     */
    @Override
    public boolean containsRole(String token, String role) {
        boolean result = false;

        if (token != null && !token.isEmpty() && role != null && !role.isEmpty()) {
            List<String> roles = getUserAuthInfo(token);

            for (String r : roles) {
                logger.info("containsRole(): user's role={}, role={}", r, role);
                if (r.equalsIgnoreCase(role)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.cashmallow.api.auth.AuthService#getUserIdByJsonWebToken(java.lang.String)
     */
    @Override
    public long getUserIdByJsonWebToken(String token) {

        try {

            Jws<Claims> jws = parseJWT(token);

            String userId = jws.getBody().get("user_id").toString();

            if (!StringUtils.isEmpty(userId)) {
                return Long.parseLong(userId);
            }

        } catch (JwtException e) {
            logger.error(e.getMessage(), e);
        }

        return Const.NO_USER_ID;

    }

    /*
     * (non-Javadoc)
     *
     * @see com.cashmallow.api.auth.AuthService#getUserId(java.lang.String)
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public long getUserId(String token) {

        long userId = Const.NO_USER_ID;

        logger.debug("token={}", token);

        UserAuth userAuth = getUserInfo(token, true);

        if (userAuth != null) {
            userId = userAuth.getUserId();
        }

        return userId;

    }

    @Transactional
    public void deleteRefreshToken(Long userId) {
        String method = "deleteRefreshToken()";

        int affectedRow = refreshTokenMapper.deleteRefreshTokenByUserId(userId);

        logger.info("{}: userId={}, affectedRow={}", method, userId, affectedRow);

        if (affectedRow != 1) {
            logger.warn("{}: There is no refresh token according to the login. affectedRow={}", method, affectedRow);
        }

    }

    /**
     * Check token is Hex String or not
     *
     * @param param
     * @return
     */
    public boolean isHexaStr(String param) {
        String token = param;
        if (token.startsWith(AuthService.TOKEN_PREFIX)) {
            token = token.replaceFirst(AuthService.TOKEN_PREFIX, "").trim();
        }
        return (token != null) && Pattern.matches("^[0-9A-Fa-f]{10,64}$", token);
    }

    public Optional<String> isValidUser(String token, HttpServletResponse response) {
        long userId = getUserId(token);
        if (userId == Const.NO_USER_ID && !isHexaStr(token)) {
            logger.info("NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return Optional.of(CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response));
        }
        return Optional.empty();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.cashmallow.api.auth.AuthService#add(com.cashmallow.api.domain.model.user.
     * AccessToken)
     */
    @Override
    @Deprecated
    public String add(AccessToken accessToken) {
        String idToken = "";

        if (accessToken != null) {
            String login = CustomStringUtil.trim(accessToken.getLogin());

            if (login.length() >= 1) {
                accessToken.setLogin(login);

                // login에 대한 token을 생성한다.
                idToken = mkToken(login);

                // login의 기 등록 여부를 검사한다.
                logger.info("add(): ***** 1 sync={}", sync);

                accessToken.setToken(idToken);

                insertAccessToken(accessToken);
            }
        }

        if (accessToken != null) {
            logger.info("add(): user_id={}, traveler_id={}, storekeeper_id={}, auths={}", accessToken.getUserId(),
                    accessToken.getTravelerId(), accessToken.getStorekeeperId(), accessToken.getAuths());
        }

        return idToken;
    }

    /**
     * (Old) Generate token
     *
     * @param login
     * @return
     */
    @Deprecated
    private static String mkToken(String login) {
        String uuid1 = UUID.randomUUID().toString().replace("-", "");
        String uuid2 = UUID.randomUUID().toString().replace("-", "");
        String uuid3 = UUID.randomUUID().toString().replace("-", "");
        String uuid = uuid1 + uuid3 + uuid2;
        return String.format("%s%f.%s", uuid, Math.random(), login);
    }

    /**
     * (Old) get access token from access_token table
     *
     * @param token
     * @return AccessToken object
     */
    @Deprecated
    private AccessToken getAccessTokenByToken(String token) {
        logger.debug("getAccessToken(): token.length={}", token.length());
        return accessTokenMapper.getAccessTokenByToken(token);
    }

    /**
     * (Old) insert access token into access_token table
     *
     * @param accessToken AccessToken object
     * @return affected row count
     */
    @Deprecated
    private int insertAccessToken(AccessToken accessToken) {
        logger.info("insertAccessToken(): login={}, accessTime={}", accessToken.getLogin(),
                accessToken.getAccessTime());
        return accessTokenMapper.insertAccessToken(accessToken);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.cashmallow.api.auth.AuthService#deleteAccessToken(java.lang.String)
     */
    @Override
    @Deprecated
    public int deleteAccessToken(String login) {
        logger.info("deleteAccessToken(): login={}", login);
        return accessTokenMapper.deleteAccessToken(login);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser(String token) {
        long userId = getUserId(token);
        if (userId == Const.NO_USER_ID) {
            return null;
        }

        return userMapper.getUserByUserId(userId);
    }

}
