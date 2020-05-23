package io.supertokens.javalin;

import com.google.gson.JsonObject;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

public class Session {

    private final String accessToken;
    private final String sessionHandle;
    private final String userId;
    private final JsonObject userDataInJWT;
    private final Context ctx;

    Session(String accessToken, String sessionHandle, String userId, JsonObject userDataInJWT, Context ctx) {
        this.accessToken = accessToken;
        this.sessionHandle = sessionHandle;
        this.userId = userId;
        this.userDataInJWT = userDataInJWT;
        this.ctx = ctx;
    }

    public void revokeSession() {
        // TODO:
    }

    public JsonObject getSessionData() {
        // TODO:
        return null;
    }

    public void updateSessionData(@NotNull JsonObject newSessionData) {
        // TODO:
    }

    public String getUserId() {
        // TODO:
        return null;
    }

    public JsonObject getJWTPayload() {
        // TODO:
        return null;
    }

    public String getSessionHandle() {
        // TODO:
        return null;
    }

    public String getAccessToken() {
        // TODO:
        return null;
    }

    public void updateJWTPayload(@NotNull JsonObject newJWTPayload) {
        // TODO:
    }
}
