package io.supertokens.javalin;

import io.javalin.http.Context;
import io.supertokens.javalin.core.exception.GeneralException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SessionBuilder {

    @NotNull
    private final Context ctx;

    @NotNull
    private final String userId;

    @NotNull
    private Map<String, Object> jwtPayload = new HashMap<>();

    @NotNull
    private Map<String, Object>  sessionData = new HashMap<>();

    SessionBuilder(@NotNull Context ctx, @NotNull String userId) {
        this.ctx = ctx;
        this.userId = userId;
    }

    public void withJWTPayload(@NotNull Map<String, Object>  jwtPayload) {
        this.jwtPayload = jwtPayload;
    }

    public void withSessionData(@NotNull Map<String, Object>  sessionData) {
        this.sessionData = sessionData;
    }

    public Session create() throws GeneralException {
        return SuperTokens.createNewSession(this.ctx, this.userId, this.jwtPayload, this.sessionData);
    }
}
