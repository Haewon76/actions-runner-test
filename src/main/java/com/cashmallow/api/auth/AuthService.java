package com.cashmallow.api.auth;

import com.cashmallow.api.domain.model.user.AccessToken;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.shared.CashmallowException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;


// 캐싱 testsetsetset2222232323233
public interface AuthService {

    static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Issue refresh token
     *
     * @param login
     * @param userId
     * @param auths
     * @return generated token:String
     */
    String issueRefreshToken(String login, Long userId, String auths) throws CashmallowException;

    /**
     * Generate new access token with refresh token
     *
     * @param refreshToken
     * @return generated new access token
     */
    String issueAccessToken(String refreshToken);

    /**
     * token 사용자의 사용자 정보를 응답한다.
     *
     * @param token
     * @return
     */
    UserAuth getUserInfo(String token);

    /**
     * token 사용자의 사용자 정보를 응답한다.
     *
     * @param token
     * @param isRenewal : token 갱신 여부. true 면 갱신하고 false 면 갱신하지 않는다.
     * @return
     */
    UserAuth getUserInfo(String token, boolean isRenewal);

    /**
     * retrieve user's role list
     *
     * @param token
     * @return user's role list : List<String>
     */
    List<String> getUserAuthInfo(String token);

    /**
     * check the user has the role by token
     *
     * @param token
     * @param role
     * @return if the user has the role then return true
     */
    boolean containsRole(String token, String role);

    /**
     * JSON Web Token 사용자의 사용자 ID(user.id)을 응답한다.
     *
     * @param token
     * @return
     */
    long getUserIdByJsonWebToken(String token);

    /**
     * token 사용자의 사용자 ID(user.id)을 응답한다. token 은 갱신된다.
     *
     * @param token
     * @return token 사용자가 존재하지 않는 경우 Const.NO_USER_ID를 응답한다.
     */
    long getUserId(String token);

    /**
     * delete refresh token
     *
     * @param token
     * @throws CashmallowException
     */
    void deleteRefreshToken(Long userId);

    /**
     * check the token is Hex string
     *
     * @param token
     * @return
     */
    boolean isHexaStr(String token);

    Optional<String> isValidUser(String token, HttpServletResponse response);

    /**
     * parse JWT
     *
     * @param token
     * @return
     */
    Jws<Claims> parseJWT(String token);

    /**
     * (Old) Generate new access token
     *
     * @param accessToken AccessToken object
     * @return generated new access token
     */
    String add(AccessToken accessToken);

    /**
     * (Old) delete access token from access_token table
     *
     * @param loginId
     * @return affected row count
     * @deprecated 2020-03-30
     */
    @Deprecated
    int deleteAccessToken(String login);

    User getUser(String token);
}