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
import io.javalin.http.Handler;
import io.supertokens.javalin.core.Utils;
import io.supertokens.javalin.core.exception.GeneralException;
import io.supertokens.javalin.core.exception.TokenTheftDetectedException;
import io.supertokens.javalin.core.exception.TryRefreshTokenException;
import io.supertokens.javalin.core.exception.UnauthorisedException;
import io.supertokens.javalin.core.HandshakeInfo;
import io.supertokens.javalin.core.informationHolders.SessionTokens;
import io.supertokens.javalin.core.SessionFunctions;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SuperTokens {

    private static final String CONTEXT_ATTRIBUTE_KEY = "SUPERTOKENS_SESSION";

    public static void config(String config) throws GeneralException {
        SessionFunctions.config(config);
    }

    public static SessionBuilder newSession(@NotNull Context ctx, @NotNull  String userId) {
        return new SessionBuilder(ctx, userId);
    }

    static Session createNewSession(@NotNull Context ctx, @NotNull  String userId,
                                    @NotNull Map<String, Object> jwtPayload,
                                    @NotNull Map<String, Object> sessionData) throws GeneralException {
        SessionTokens sessionTokens = SessionFunctions.createNewSession(userId, Utils.mapToJsonObject(jwtPayload),
                Utils.mapToJsonObject(sessionData));

        CookieAndHeaders.attachAccessTokenToCookie(ctx, sessionTokens.accessToken);
        CookieAndHeaders.attachRefreshTokenToCookie(ctx, sessionTokens.refreshToken);
        CookieAndHeaders.setIdRefreshTokenInHeaderAndCookie(ctx, sessionTokens.idRefreshToken);
        if (sessionTokens.antiCsrfToken != null) {
            CookieAndHeaders.setAntiCsrfTokenInHeaders(ctx, sessionTokens.antiCsrfToken);
        }
        return new Session(sessionTokens.accessToken.token, sessionTokens.handle,
                sessionTokens.userId, Utils.jsonObjectToMap(sessionTokens.userDataInJWT), ctx);
    }

    public static Session getSession(@NotNull Context ctx, boolean doAntiCSRFCheck)
            throws TryRefreshTokenException, UnauthorisedException, GeneralException {
        CookieAndHeaders.saveFrontendInfoFromRequest(ctx);

        String accessToken = CookieAndHeaders.getAccessTokenFromCookie(ctx);
        if (accessToken == null) {
            throw new TryRefreshTokenException("access token missing in cookies");
        }
        try {
            String antiCsrfToken = CookieAndHeaders.getAntiCSRFTokenFromHeaders(ctx);
            String idRefreshToken = CookieAndHeaders.getIdRefreshTokenFromCookie(ctx);
            SessionTokens response = SessionFunctions.getSession(accessToken, antiCsrfToken, doAntiCSRFCheck, idRefreshToken);
            if (response.accessToken != null) {
                CookieAndHeaders.attachAccessTokenToCookie(ctx, response.accessToken);
                accessToken = response.accessToken.token;
            }
            return new Session(accessToken, response.handle, response.userId, Utils.jsonObjectToMap(response.userDataInJWT), ctx);
        } catch (UnauthorisedException e) {
            HandshakeInfo handShakeInfo = HandshakeInfo.getInstance();
            CookieAndHeaders.clearSessionFromCookie(
                    ctx,
                    handShakeInfo.cookieDomain,
                    handShakeInfo.cookieSecure,
                    handShakeInfo.accessTokenPath,
                    handShakeInfo.refreshTokenPath,
                    handShakeInfo.idRefreshTokenPath,
                    handShakeInfo.cookieSameSite);
            throw e;
        }
    }

    public static Session refreshSession(@NotNull Context ctx)
            throws UnauthorisedException, TokenTheftDetectedException, GeneralException {
        CookieAndHeaders.saveFrontendInfoFromRequest(ctx);
        String inputRefreshToken = CookieAndHeaders.getRefreshTokenFromCookie(ctx);
        if (inputRefreshToken == null) {
            HandshakeInfo handShakeInfo = HandshakeInfo.getInstance();
            CookieAndHeaders.clearSessionFromCookie(
                    ctx,
                    handShakeInfo.cookieDomain,
                    handShakeInfo.cookieSecure,
                    handShakeInfo.accessTokenPath,
                    handShakeInfo.refreshTokenPath,
                    handShakeInfo.idRefreshTokenPath,
                    handShakeInfo.cookieSameSite);
            throw new UnauthorisedException("Missing auth tokens in cookies. Have you set the correct refresh API path in your frontend and SuperTokens config?");
        }
        try {

            SessionTokens sessionTokens = SessionFunctions.refreshSession(inputRefreshToken);
            CookieAndHeaders.attachAccessTokenToCookie(ctx, sessionTokens.accessToken);
            CookieAndHeaders.attachRefreshTokenToCookie(ctx, sessionTokens.refreshToken);
            CookieAndHeaders.setIdRefreshTokenInHeaderAndCookie(ctx, sessionTokens.idRefreshToken);
            if (sessionTokens.antiCsrfToken != null) {
                CookieAndHeaders.setAntiCsrfTokenInHeaders(ctx, sessionTokens.antiCsrfToken);
            }
            return new Session(sessionTokens.accessToken.token, sessionTokens.handle,
                    sessionTokens.userId, Utils.jsonObjectToMap(sessionTokens.userDataInJWT), ctx);

        } catch (UnauthorisedException | TokenTheftDetectedException e) {
            HandshakeInfo handShakeInfo = HandshakeInfo.getInstance();
            CookieAndHeaders.clearSessionFromCookie(
                    ctx,
                    handShakeInfo.cookieDomain,
                    handShakeInfo.cookieSecure,
                    handShakeInfo.accessTokenPath,
                    handShakeInfo.refreshTokenPath,
                    handShakeInfo.idRefreshTokenPath,
                    handShakeInfo.cookieSameSite);
            throw e;
        }
    }

    public static String[] revokeAllSessionsForUser(@NotNull String userId) throws GeneralException {
        return SessionFunctions.revokeAllSessionsForUser(userId);
    }

    public static String[] getAllSessionHandlesForUser(@NotNull String userId) throws GeneralException {
        return SessionFunctions.getAllSessionHandlesForUser(userId);
    }

    public static boolean revokeSession(@NotNull String sessionHandle) throws GeneralException {
        return SessionFunctions.revokeSession(sessionHandle);
    }

    public static String[] revokeMultipleSessions(@NotNull String[] sessionHandles) throws GeneralException {
        return SessionFunctions.revokeMultipleSessions(sessionHandles);
    }

    public static Map<String, Object> getSessionData(@NotNull String sessionHandle)
            throws GeneralException, UnauthorisedException {
        return Utils.jsonObjectToMap(SessionFunctions.getSessionData(sessionHandle));
    }

    public static void updateSessionData(@NotNull String sessionHandle, @NotNull Map<String, Object> sessionData)
            throws GeneralException, UnauthorisedException {
        SessionFunctions.updateSessionData(sessionHandle, Utils.mapToJsonObject(sessionData));
    }

    public static void setRelevantHeadersForOptionsAPI(@NotNull Context ctx) {
        CookieAndHeaders.setOptionsAPIHeader(ctx);
    }

    public static Map<String, Object> getJWTPayload(@NotNull String sessionHandle) throws GeneralException,
            UnauthorisedException {
        return Utils.jsonObjectToMap(SessionFunctions.getJWTPayload(sessionHandle));
    }

    public static void updateJWTPayload(@NotNull String sessionHandle, @NotNull Map<String, Object> newJWTPayload)
            throws GeneralException, UnauthorisedException {
        SessionFunctions.updateJWTPayload(sessionHandle, Utils.mapToJsonObject(newJWTPayload));
    }

    // -----------------------------------------

    public static Session getFromContext(Context ctx) {
        return ctx.attribute(CONTEXT_ATTRIBUTE_KEY);
    }

    public static Handler middleware() {
        return middleware(null);
    }

    public static Handler middleware(final Boolean antiCsrfCheck) {
        return ctx -> {
            Boolean antiCsrfCheckInLambda = antiCsrfCheck;
            if (ctx.req.getMethod().equalsIgnoreCase("options") ||
                    ctx.req.getMethod().equalsIgnoreCase("trace")) {
                return;
            }
            String path = ctx.path().split("\\?")[0];
            HandshakeInfo handshakeInfo = HandshakeInfo.getInstance();
            if (
                    (handshakeInfo.refreshTokenPath.equals(path)) ||
                    (handshakeInfo.refreshTokenPath.equals(path + "/")) ||
                    ((handshakeInfo.refreshTokenPath + "/").equals(path))
                    &&
                    ctx.req.getMethod().equalsIgnoreCase("post")
            ) {
                ctx.attribute(CONTEXT_ATTRIBUTE_KEY, refreshSession(ctx));
            } else {
                if (antiCsrfCheckInLambda == null) {
                    antiCsrfCheckInLambda = !ctx.req.getMethod().equalsIgnoreCase("get");
                }
                ctx.attribute(CONTEXT_ATTRIBUTE_KEY, getSession(ctx, antiCsrfCheckInLambda));
            }
        };
    }

    public static SuperTokensExceptionHandler exceptionHandler() {
        return new SuperTokensExceptionHandler();
    }

}