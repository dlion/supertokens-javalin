package io.supertokens.javalin.core.informationHolders;

import com.google.gson.JsonObject;

public class SessionTokens {
    public final String handle;
    public final String userId;
    public final JsonObject userDataInJWT;
    public final TokenInfo accessToken;
    public final TokenInfo refreshToken;
    public final TokenInfo idRefreshToken;
    public final String antiCsrfToken;

    public SessionTokens(String handle, String userId, JsonObject userDataInJWT, TokenInfo accessToken,
                         TokenInfo refreshToken, TokenInfo idRefreshToken, String antiCsrfToken) {
        this.handle = handle;
        this.userId = userId;
        this.userDataInJWT = userDataInJWT;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.idRefreshToken = idRefreshToken;
        this.antiCsrfToken = antiCsrfToken;
    }
}
