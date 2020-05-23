package io.supertokens.javalin;

import com.google.gson.JsonObject;
import io.javalin.http.Context;
import io.supertokens.javalin.core.Exception.GeneralException;
import io.supertokens.javalin.core.Exception.UnauthorisedException;
import io.supertokens.javalin.core.HandshakeInfo;
import io.supertokens.javalin.core.InformationHolders.SessionTokens;
import io.supertokens.javalin.core.SessionFunctions;
import org.jetbrains.annotations.NotNull;

public class Session {

    private String accessToken;
    private String sessionHandle;
    private String userId;
    private JsonObject userDataInJWT;
    private Context ctx;

    Session(String accessToken, String sessionHandle, String userId, JsonObject userDataInJWT, Context ctx) {
        this.accessToken = accessToken;
        this.sessionHandle = sessionHandle;
        this.userId = userId;
        this.userDataInJWT = userDataInJWT;
        this.ctx = ctx;
    }

    public void revokeSession() throws GeneralException {
        if (SessionFunctions.revokeSession(this.sessionHandle)) {
            HandshakeInfo handShakeInfo = HandshakeInfo.getInstance();
            CookieAndHeaders.clearSessionFromCookie(
                    ctx,
                    handShakeInfo.cookieDomain,
                    handShakeInfo.cookieSecure,
                    handShakeInfo.accessTokenPath,
                    handShakeInfo.refreshTokenPath,
                    handShakeInfo.idRefreshTokenPath,
                    handShakeInfo.cookieSameSite
            );
        }
    }

    public JsonObject getSessionData() throws GeneralException, UnauthorisedException {
        try {
            return SessionFunctions.getSessionData(this.sessionHandle);
        } catch (UnauthorisedException err) {
            HandshakeInfo handShakeInfo = HandshakeInfo.getInstance();
            CookieAndHeaders.clearSessionFromCookie(
                    ctx,
                    handShakeInfo.cookieDomain,
                    handShakeInfo.cookieSecure,
                    handShakeInfo.accessTokenPath,
                    handShakeInfo.refreshTokenPath,
                    handShakeInfo.idRefreshTokenPath,
                    handShakeInfo.cookieSameSite
            );
            throw err;
        }
    }

    public void updateSessionData(@NotNull JsonObject newSessionData) throws GeneralException, UnauthorisedException {
        try {
            SessionFunctions.updateSessionData(this.sessionHandle, newSessionData);
        } catch (UnauthorisedException err) {
            HandshakeInfo handShakeInfo = HandshakeInfo.getInstance();
            CookieAndHeaders.clearSessionFromCookie(
                    ctx,
                    handShakeInfo.cookieDomain,
                    handShakeInfo.cookieSecure,
                    handShakeInfo.accessTokenPath,
                    handShakeInfo.refreshTokenPath,
                    handShakeInfo.idRefreshTokenPath,
                    handShakeInfo.cookieSameSite
            );
            throw err;
        }
    }

    public String getUserId() {
        return this.userId;
    }

    public JsonObject getJWTPayload() {
        return this.userDataInJWT;
    }

    public String getSessionHandle() {
        return this.sessionHandle;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void updateJWTPayload(@NotNull JsonObject newJWTPayload) throws UnauthorisedException, GeneralException {
        try {
            SessionTokens sessionTokens = SessionFunctions.regenerateSession(this.accessToken, newJWTPayload);
            this.userDataInJWT = sessionTokens.userDataInJWT;
            if (sessionTokens.accessToken != null) {
                this.accessToken = sessionTokens.accessToken.token;
                CookieAndHeaders.attachAccessTokenToCookie(this.ctx, sessionTokens.accessToken);
            }
        } catch ( UnauthorisedException e) {
            HandshakeInfo handShakeInfo = HandshakeInfo.getInstance();
            CookieAndHeaders.clearSessionFromCookie(
                    ctx,
                    handShakeInfo.cookieDomain,
                    handShakeInfo.cookieSecure,
                    handShakeInfo.accessTokenPath,
                    handShakeInfo.refreshTokenPath,
                    handShakeInfo.idRefreshTokenPath,
                    handShakeInfo.cookieSameSite
            );
            throw e;
        }

    }
}
