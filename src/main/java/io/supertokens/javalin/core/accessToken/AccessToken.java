package io.supertokens.javalin.core.accessToken;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.supertokens.javalin.core.exception.TryRefreshTokenException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AccessToken {
    public static AccessTokenInfo getInfoFromAccessToken(String token, String jwtSigningPublicKey, boolean doAntiCsrfCheck)
            throws TryRefreshTokenException {
        try {
            JsonObject payload = JWT.verifyJWTAndGetPayload(token, jwtSigningPublicKey);
            AccessTokenInfo tokenInfo = new Gson().fromJson(payload, AccessTokenInfo.class);
            if (tokenInfo.sessionHandle == null || tokenInfo.userId == null || tokenInfo.refreshTokenHash1 == null
                    || tokenInfo.userData == null || (doAntiCsrfCheck && tokenInfo.antiCsrfToken == null)) {
                throw new TryRefreshTokenException(
                        "Access token does not contain all the information. Maybe the structure has changed?");
            }

            if (tokenInfo.expiryTime < System.currentTimeMillis()) {
                throw new TryRefreshTokenException("Access token expired");
            }
            return tokenInfo;
        } catch (InvalidKeyException | NoSuchAlgorithmException | JWT.JWTException e) {
            throw new TryRefreshTokenException("Error while verifying JWT");
        }

    }

    public static class AccessTokenInfo {
        public final String sessionHandle;
        public final String userId;
        public final String refreshTokenHash1;
        public final String parentRefreshTokenHash1;
        public final JsonObject userData;
        public final String antiCsrfToken;
        public final long expiryTime;
        final long timeCreated;

        AccessTokenInfo(String sessionHandle, String userId,  String refreshTokenHash1,
                        long expiryTime, String parentRefreshTokenHash1, JsonObject userData,
                        String antiCsrfToken, long timeCreated, boolean isPaid) {
            this.sessionHandle = sessionHandle;
            this.userId = userId;
            this.refreshTokenHash1 = refreshTokenHash1;
            this.expiryTime = expiryTime;
            this.parentRefreshTokenHash1 = parentRefreshTokenHash1;
            this.userData = userData;
            this.antiCsrfToken = antiCsrfToken;
            this.timeCreated = timeCreated;
        }
    }
}
