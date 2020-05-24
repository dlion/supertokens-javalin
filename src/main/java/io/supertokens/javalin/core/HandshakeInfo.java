package io.supertokens.javalin.core;

import com.google.gson.JsonObject;
import io.supertokens.javalin.core.Exception.GeneralException;

public class HandshakeInfo {

    private static HandshakeInfo instance = null;

    public  String jwtSigningPublicKey;
    public  String cookieDomain;
    public  boolean cookieSecure;
    public  String accessTokenPath;
    public  String refreshTokenPath;
    public  boolean enableAntiCsrf;
    public  boolean accessTokenBlacklistingEnabled;
    public  long jwtSigningPublicKeyExpiryTime;
    public  String cookieSameSite;
    public  String idRefreshTokenPath;
    public  int sessionExpiredStatusCode;

    public synchronized static HandshakeInfo getInstance() throws GeneralException {
        if (HandshakeInfo.instance == null) {
            JsonObject response = new JsonObject(); // TODO: send request
            HandshakeInfo.instance = new HandshakeInfo(
                    response.get("jwtSigningPublicKey").getAsString(),
                    response.get("cookieDomain").getAsString(),
                    response.get("cookieSecure").getAsBoolean(),
                    response.get("accessTokenPath").getAsString(),
                    response.get("refreshTokenPath").getAsString(),
                    response.get("enableAntiCsrf").getAsBoolean(),
                    response.get("accessTokenBlacklistingEnabled").getAsBoolean(),
                    response.get("jwtSigningPublicKeyExpiryTime").getAsLong(),
                    response.get("cookieSameSite").getAsString(),
                    response.get("idRefreshTokenPath").getAsString(),
                    response.get("sessionExpiredStatusCode").getAsInt());
        }
        return HandshakeInfo.instance;
    }

    private HandshakeInfo(
            String jwtSigningPublicKey,
            String cookieDomain,
            boolean cookieSecure,
            String accessTokenPath,
            String refreshTokenPath,
            boolean enableAntiCsrf,
            boolean accessTokenBlacklistingEnabled,
            long jwtSigningPublicKeyExpiryTime,
            String cookieSameSite,
            String idRefreshTokenPath,
            int sessionExpiredStatusCode
    ) {
        this.jwtSigningPublicKey = jwtSigningPublicKey;
        this.cookieDomain = cookieDomain;
        this.cookieSecure = cookieSecure;
        this.accessTokenPath = accessTokenPath;
        this.refreshTokenPath = refreshTokenPath;
        this.enableAntiCsrf = enableAntiCsrf;
        this.accessTokenBlacklistingEnabled = accessTokenBlacklistingEnabled;
        this.jwtSigningPublicKeyExpiryTime = jwtSigningPublicKeyExpiryTime;
        this.cookieSameSite = cookieSameSite;
        this.idRefreshTokenPath = idRefreshTokenPath;
        this.sessionExpiredStatusCode = sessionExpiredStatusCode;
    }

    public void updateJwtSigningPublicKeyInfo(String newKey, long newExpiry) {
        synchronized (HandshakeInfo.class) {
            this.jwtSigningPublicKey = newKey;
            this.jwtSigningPublicKeyExpiryTime = newExpiry;
        }
    };
}
