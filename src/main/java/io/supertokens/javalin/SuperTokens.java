package io.supertokens.javalin;

import com.google.gson.JsonObject;
import io.javalin.http.Context;
import io.supertokens.javalin.core.Exception.GeneralException;
import io.supertokens.javalin.core.Exception.TokenTheftDetectedException;
import io.supertokens.javalin.core.Exception.TryRefreshTokenException;
import io.supertokens.javalin.core.Exception.UnauthorisedException;
import io.supertokens.javalin.core.HandshakeInfo;
import io.supertokens.javalin.core.InformationHolders.SessionTokens;
import io.supertokens.javalin.core.SessionFunctions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuperTokens {

    public static SessionBuilder newSession(@NotNull Context ctx, @NotNull  String userId) {
        return new SessionBuilder(ctx, userId);
    }

    static Session createNewSession(@NotNull Context ctx, @NotNull  String userId,
                                    @Nullable JsonObject jwtPayload,
                                    @Nullable JsonObject sessionData) {
        SessionTokens sessionTokens = SessionFunctions.createNewSession(userId, jwtPayload, sessionData);

        CookieAndHeaders.attachAccessTokenToCookie(ctx, sessionTokens.accessToken);
        CookieAndHeaders.attachRefreshTokenToCookie(ctx, sessionTokens.refreshToken);
        CookieAndHeaders.setIdRefreshTokenInHeaderAndCookie(ctx, sessionTokens.idRefreshToken);
        if (sessionTokens.antiCsrfToken != null) {
            CookieAndHeaders.setAntiCsrfTokenInHeaders(ctx, sessionTokens.antiCsrfToken);
        }
        return new Session(sessionTokens.accessToken.token, sessionTokens.handle,
                sessionTokens.userId, sessionTokens.userDataInJWT, ctx);
    }

    public static Session getSession(@NotNull Context ctx, boolean doAntiCSRFCheck)
            throws TryRefreshTokenException, UnauthorisedException {
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
            return new Session(accessToken, response.handle, response.userId, response.userDataInJWT, ctx);
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

    public static Session refreshSession(@NotNull Context ctx) throws UnauthorisedException, TokenTheftDetectedException {
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
                    sessionTokens.userId, sessionTokens.userDataInJWT, ctx);

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

    public static JsonObject getSessionData(@NotNull String sessionHandle)
            throws GeneralException, UnauthorisedException {
        return SessionFunctions.getSessionData(sessionHandle);
    }

    public static void updateSessionData(@NotNull String sessionHandle, @NotNull JsonObject sessionData)
            throws GeneralException, UnauthorisedException {
        SessionFunctions.updateSessionData(sessionHandle, sessionData);
    }

    public static void setRelevantHeadersForOptionsAPI(@NotNull Context ctx) {
        CookieAndHeaders.setOptionsAPIHeader(ctx);
    }

    public static JsonObject getJWTPayload(@NotNull String sessionHandle) throws GeneralException,
            UnauthorisedException {
        return SessionFunctions.getJWTPayload(sessionHandle);
    }

    public static void updateJWTPayload(@NotNull String sessionHandle, @NotNull JsonObject newJWTPayload)
            throws GeneralException, UnauthorisedException {
        SessionFunctions.updateJWTPayload(sessionHandle, newJWTPayload);
    }

}