package io.supertokens.javalin;

import io.javalin.http.Context;
import io.supertokens.javalin.core.Utils;
import io.supertokens.javalin.core.exception.GeneralException;
import io.supertokens.javalin.core.exception.UnauthorisedException;
import io.supertokens.javalin.core.HandshakeInfo;
import io.supertokens.javalin.core.informationHolders.SessionTokens;
import io.supertokens.javalin.core.SessionFunctions;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Session {

    private String accessToken;
    private String sessionHandle;
    private String userId;
    private Map<String, Object> userDataInJWT;
    private Context ctx;

    Session(String accessToken, String sessionHandle, String userId, Map<String, Object>  userDataInJWT, Context ctx) {
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

    public Map<String, Object>  getSessionData() throws GeneralException, UnauthorisedException {
        try {
            return Utils.jsonObjectToMap(SessionFunctions.getSessionData(this.sessionHandle));
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

    public void updateSessionData(@NotNull Map<String, Object>  newSessionData) throws GeneralException, UnauthorisedException {
        try {
            SessionFunctions.updateSessionData(this.sessionHandle, Utils.mapToJsonObject(newSessionData));
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

    public Map<String, Object>  getJWTPayload() {
        return this.userDataInJWT;
    }

    public String getSessionHandle() {
        return this.sessionHandle;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void updateJWTPayload(@NotNull Map<String, Object>  newJWTPayload) throws UnauthorisedException, GeneralException {
        try {
            SessionTokens sessionTokens = SessionFunctions.regenerateSession(this.accessToken,
                    Utils.mapToJsonObject(newJWTPayload));
            this.userDataInJWT = Utils.jsonObjectToMap(sessionTokens.userDataInJWT);
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
