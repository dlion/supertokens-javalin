package io.supertokens.javalin.core;

import com.google.gson.JsonObject;
import io.supertokens.javalin.core.InformationHolders.SessionTokens;
import io.supertokens.javalin.core.InformationHolders.TokenInfo;

import java.util.*;

public class Utils {

    public static SessionTokens parseJsonResponse(JsonObject response) {
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

    public static String getLargestVersionFromIntersection(String[] v1, String[] v2) {
        Set<String> v2Set = new HashSet<>(Arrays.asList(v2));
        List<String> intesection = new ArrayList<>();
        for (String s : v1) {
            if (v2Set.contains(s)) {
                intesection.add(s);
            }
        }
        if (intesection.size() == 0) {
            return null;
        }
        String maxSoFar = intesection.get(0);
        for (String s : intesection) {
            maxSoFar = maxVersion(s, maxSoFar);
        }
        return maxSoFar;
    }

    private static String maxVersion(String version1, String version2) {
        String[] splittedV1 = version1.split("\\.");
        String[] splittedV2 = version2.split("\\.");
        int minLength = Math.min(splittedV1.length, splittedV2.length);
        for (int i = 0; i < minLength; i++) {
            int v1 = Integer.parseInt(splittedV1[i]);
            int v2 = Integer.parseInt(splittedV2[i]);
            if (v1 > v2) {
                return version1;
            } else if (v2 > v1) {
                return version2;
            }
        }
        if (splittedV1.length >= splittedV2.length) {
            return version1;
        }
        return version2;
    }
}
