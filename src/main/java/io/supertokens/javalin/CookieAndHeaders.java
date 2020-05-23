package io.supertokens.javalin;

import io.javalin.http.Context;
import io.supertokens.javalin.core.InformationHolders.TokenInfo;

public class CookieAndHeaders {

    public static void saveFrontendInfoFromRequest(Context ctx) {
        // TODO:
    }

    public static void clearSessionFromCookie(Context ctx, String domain, boolean secure, String accessTokenPath,
                                              String refreshTokenPath, String idRefreshTokenPath, String sameSite) {
        // TODO:
    }

    public static void attachAccessTokenToCookie(Context ctx, TokenInfo token) {
        // TODO:
    }

    public static void attachRefreshTokenToCookie(Context ctx, TokenInfo token) {
        // TODO:
    }

    public static String getAccessTokenFromCookie(Context ctx) {
        // TODO:
        return null;
    }

    public static String getRefreshTokenFromCookie(Context ctx) {
        // TODO:
        return null;
    }

    public static String getAntiCSRFTokenFromHeaders(Context ctx) {
        // TODO:
        return null;
    }

    public static String getIdRefreshTokenFromCookie(Context ctx) {
        // TODO:
        return null;
    }

    public static void setAntiCsrfTokenInHeaders(Context context, String antiCsrfToken) {
        // TODO:
    }

    public static void setIdRefreshTokenInHeaderAndCookie(Context ctx, TokenInfo token) {
        // TODO:
    }

    public static void setOptionsAPIHeader(Context ctx) {
        // TODO:
    }
}
