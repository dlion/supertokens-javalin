package io.supertokens.javalin.core;

import com.google.gson.JsonObject;
import io.supertokens.javalin.core.InformationHolders.SessionTokens;
import io.supertokens.javalin.core.InformationHolders.TokenInfo;

class Utils {

    static SessionTokens parseJsonResponse(JsonObject response) {
        JsonObject sessionJson = response.getAsJsonObject("session");
        JsonObject accessTokenJson = response.getAsJsonObject("accessToken");
        JsonObject refreshTokenJson = response.getAsJsonObject("refreshToken");
        JsonObject idRefreshTokenJson = response.getAsJsonObject("idRefreshToken");
        String antiCSRFToken = response.get("antiCsrfToken") == null ? null :
                response.get("antiCsrfToken").getAsString();

        TokenInfo accessToken = null;
        if (accessTokenJson != null) {
            accessToken = new TokenInfo(
                    accessTokenJson.get("token").getAsString(),
                    accessTokenJson.get("expiry").getAsLong(),
                    accessTokenJson.get("createdTime").getAsLong(),
                    accessTokenJson.get("cookiePath").getAsString(),
                    accessTokenJson.get("cookieSecure").getAsBoolean(),
                    accessTokenJson.get("domain").getAsString(),
                    accessTokenJson.get("sameSite").getAsString());
        }
        TokenInfo refreshToken = null;
        if (refreshTokenJson != null) {
            refreshToken = new TokenInfo(
                    refreshTokenJson.get("token").getAsString(),
                    refreshTokenJson.get("expiry").getAsLong(),
                    refreshTokenJson.get("createdTime").getAsLong(),
                    refreshTokenJson.get("cookiePath").getAsString(),
                    refreshTokenJson.get("cookieSecure").getAsBoolean(),
                    refreshTokenJson.get("domain").getAsString(),
                    refreshTokenJson.get("sameSite").getAsString());
        }
        TokenInfo idRefreshToken = null;
        if (idRefreshTokenJson != null) {
            idRefreshToken = new TokenInfo(
                    idRefreshTokenJson.get("token").getAsString(),
                    idRefreshTokenJson.get("expiry").getAsLong(),
                    idRefreshTokenJson.get("createdTime").getAsLong(),
                    idRefreshTokenJson.get("cookiePath").getAsString(),
                    idRefreshTokenJson.get("cookieSecure").getAsBoolean(),
                    idRefreshTokenJson.get("domain").getAsString(),
                    idRefreshTokenJson.get("sameSite").getAsString());
        }
        return new SessionTokens(
                sessionJson.get("handle").getAsString(),
                sessionJson.get("userId").getAsString(),
                sessionJson.getAsJsonObject("userDataInJWT"),
                accessToken, refreshToken, idRefreshToken, antiCSRFToken);
    }
}
