package io.supertokens.javalin;


import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SuperTokens {

    public Session createNewSession(@NotNull Context ctx, @NotNull  String userId,
                                 @Nullable  Map jwtPayload, @Nullable Map sessionData) {
        // TODO:
        return null;
    }

    public Session getSession(@NotNull Context ctx, boolean doAntiCSRFCheck) {
        // TODO:
        return null;
    }

    public Session refreshSession(@NotNull Context ctx) {
        // TODO:
        return null;
    }

    public int revokeAllSessionsForUser(@NotNull String userId) {
        // TODO:
        return 0;
    }

    public String[] getAllSessionHandlesForUser(@NotNull String userId) {
        // TODO:
        return new String[]{};
    }


}
