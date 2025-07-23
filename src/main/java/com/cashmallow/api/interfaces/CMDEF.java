package com.cashmallow.api.interfaces;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@SuppressWarnings({"unchecked", "deprecation"})
public final class CMDEF {

    private static Logger logger = LoggerFactory.getLogger(CMDEF.class);

    /**
     * set value into session
     *
     * @param request
     * @param key
     * @param value
     * @deprecated 2020-05-07
     */
    @Deprecated
    @SuppressWarnings({"unchecked", "deprecation"})
    public static void setValueIntoSession(HttpServletRequest request, String key, String value) {
        HttpSession session = request.getSession(true);
        if (value != null) {
            session.setAttribute(key, value);
        } else if (session.getAttribute(key) != null) {
            session.removeAttribute(key);
        }
    }

    /**
     * get value from session
     *
     * @param request
     * @param key
     * @return
     * @deprecated 2020-05-07
     */
    @Deprecated
    @SuppressWarnings({"unchecked", "deprecation"})
    public static String getValueFromSession(HttpServletRequest request, String key) {
        HttpSession session = request.getSession(true);
        if (session.getAttribute(key) != null) {
            return session.getAttribute(key).toString();
        }
        return null;
    }

    // edited by kgy 20171017 : 기존의 token 베이스 연결에 토큰을 세션에 저장해 놓고 사용하자. 보안상 적용해야 한다. 기존코드가 session base 가 아니므로 이렇게라도 하자.
    private static final boolean USETOKENSESSIONSTORED = true;

    /**
     * store token into session
     *
     * @param request
     * @param voResult
     * @deprecated 2020-05-07
     */
    @Deprecated
    @SuppressWarnings({"unchecked", "deprecation"})
    public static void storeTokenIntoSession(HttpServletRequest request, ApiResultVO voResult) {
        if (!USETOKENSESSIONSTORED) {
            return;
        }

        HttpSession session = request.getSession(true);
        if (voResult.chkOk() && voResult.getObj() != null) {
            session.setAttribute("token", voResult.getObj());
        } else {
            session.removeAttribute("token");
        }
    }

    /**
     * store token into session
     *
     * @param request
     * @param accessToken
     * @deprecated 2020-05-07
     */
    @Deprecated
    @SuppressWarnings({"unchecked", "deprecation"})
    public static void storeTokenIntoSession(HttpServletRequest request, String accessToken) {
        if (!USETOKENSESSIONSTORED) {
            return;
        }

        HttpSession session = request.getSession(true);

        if (StringUtils.isEmpty(accessToken)) {
            session.removeAttribute("access_token");
        } else {
            session.setAttribute("access_token", accessToken);
        }
    }

}
