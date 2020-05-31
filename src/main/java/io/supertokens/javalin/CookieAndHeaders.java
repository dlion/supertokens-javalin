/*
 * Copyright (c) 2020, VRAI Labs and/or its affiliates. All rights reserved.
 *
 * This software is licensed under the Apache License, Version 2.0 (the
 * "License") as published by the Apache Software Foundation.
 *
 * You may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.supertokens.javalin;

import io.javalin.http.Context;
import io.supertokens.javalin.core.DeviceInfo;
import io.supertokens.javalin.core.informationHolders.TokenInfo;
import org.eclipse.jetty.http.HttpCookie;

import javax.servlet.http.Cookie;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

class CookieAndHeaders {

    private static final String accessTokenCookieKey = "sAccessToken";
    private static final String refreshTokenCookieKey = "sRefreshToken";

    private static final String idRefreshTokenCookieKey = "sIdRefreshToken";
    private static final String idRefreshTokenHeaderKey = "id-refresh-token";

    private static final String antiCsrfHeaderKey = "anti-csrf";
    private static final String frontendSDKNameHeaderKey = "supertokens-sdk-name";
    private static final String frontendSDKVersionHeaderKey = "supertokens-sdk-version";

    private static void setCookie(Context ctx, String name, String value, String domain, boolean secure,
                           boolean httpOnly, long expires, String path, String sameSite) {
        Cookie cookie = new Cookie(name, URLEncoder.encode(value, StandardCharsets.UTF_8));
        cookie.setDomain(domain);
        cookie.setSecure(secure);
        cookie.setHttpOnly(httpOnly);
        int expiry = Math.max(0, (int)Math.ceil(((expires - System.currentTimeMillis())/1000.0)));
        cookie.setMaxAge(expiry);
        cookie.setPath(path);

        if (sameSite.equals("none")) {
            cookie.setComment(HttpCookie.SAME_SITE_NONE_COMMENT);
        } else if (sameSite.equals("lax")) {
            cookie.setComment(HttpCookie.SAME_SITE_LAX_COMMENT);
        } else {
            cookie.setComment(HttpCookie.SAME_SITE_STRICT_COMMENT);
        }
        ctx.res.addCookie(cookie);
    }

    private static void setHeader(Context ctx, String key, String value) {
        String existing = ctx.res.getHeader(key);
        if (existing == null) {
            ctx.res.setHeader(key, value);
        } else {
            ctx.res.setHeader(key, existing + ", " + value);
        }
    }

    private static String getCookieValue(Context ctx, String key) {
        String value = ctx.cookie(key);
        if (value != null) {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        }
        return null;
    }

    private static String getHeader(Context ctx, String key) {
        return ctx.header(key);
    }

    static void saveFrontendInfoFromRequest(Context ctx) {
        try {
            String name = getHeader(ctx, frontendSDKNameHeaderKey);
            String version = getHeader(ctx, frontendSDKVersionHeaderKey);
            if (name != null && version != null) {
                DeviceInfo.getInstance().addToFrontendSDKs(new DeviceInfo.Device(name, version));
            }
        } catch (Exception ignored) {}
    }

    static void clearSessionFromCookie(Context ctx, String domain, boolean secure, String accessTokenPath,
                                       String refreshTokenPath, String idRefreshTokenPath, String sameSite) {
        setCookie(ctx, accessTokenCookieKey, "", domain, secure, true, 0, accessTokenPath, sameSite);
        setCookie(ctx, refreshTokenCookieKey, "", domain, secure, true, 0, refreshTokenPath, sameSite);
        setCookie(ctx, idRefreshTokenCookieKey, "", domain, secure, true, 0, idRefreshTokenPath, sameSite);
        setHeader(ctx, idRefreshTokenHeaderKey, "remove");
        setHeader(ctx, "Access-Control-Expose-Headers", idRefreshTokenHeaderKey);
    }

    static void attachAccessTokenToCookie(Context ctx, TokenInfo token) {
        setCookie(ctx, accessTokenCookieKey, token.token, token.domain, token.cookieSecure, true,
                token.expiry, token.cookiePath, token.sameSite);
    }

    static void attachRefreshTokenToCookie(Context ctx, TokenInfo token) {
        setCookie(ctx, refreshTokenCookieKey, token.token, token.domain, token.cookieSecure, true,
                token.expiry, token.cookiePath, token.sameSite);
    }

    static String getAccessTokenFromCookie(Context ctx) {
        return getCookieValue(ctx, accessTokenCookieKey);
    }

    static String getRefreshTokenFromCookie(Context ctx) {
        return getCookieValue(ctx, refreshTokenCookieKey);
    }

    static String getAntiCSRFTokenFromHeaders(Context ctx) {
        return getHeader(ctx, antiCsrfHeaderKey);
    }

    static String getIdRefreshTokenFromCookie(Context ctx) {
        return getCookieValue(ctx, idRefreshTokenCookieKey);
    }

    static void setAntiCsrfTokenInHeaders(Context ctx, String antiCsrfToken) {
        setHeader(ctx, antiCsrfHeaderKey, antiCsrfToken);
        setHeader(ctx, "Access-Control-Expose-Headers", antiCsrfHeaderKey);
    }

    static void setIdRefreshTokenInHeaderAndCookie(Context ctx, TokenInfo token) {
        setHeader(ctx, idRefreshTokenHeaderKey, token.token + ";" + token.expiry);
        setHeader(ctx, "Access-Control-Expose-Headers", idRefreshTokenHeaderKey);

        setCookie(ctx, idRefreshTokenCookieKey, token.token, token.domain, token.cookieSecure,
                true, token.expiry, token.cookiePath, token.sameSite);
    }

    static void setOptionsAPIHeader(Context ctx) {
        setHeader(ctx, "Access-Control-Allow-Headers", antiCsrfHeaderKey);
        setHeader(ctx, "Access-Control-Allow-Headers", frontendSDKNameHeaderKey);
        setHeader(ctx, "Access-Control-Allow-Headers", frontendSDKVersionHeaderKey);
        setHeader(ctx, "Access-Control-Allow-Credentials", "true");
    }
}
