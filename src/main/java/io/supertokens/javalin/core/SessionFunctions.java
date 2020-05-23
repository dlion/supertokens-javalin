package io.supertokens.javalin.core;

import com.google.gson.JsonObject;
import io.supertokens.javalin.core.InformationHolders.SessionTokens;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*This can be moved to a separate package and be shared with other java webservers.*/

public class SessionFunctions {

    public static SessionTokens createNewSession(@NotNull String userId, @Nullable JsonObject jwtPayload,
                                                 @Nullable JsonObject sessionData) {
        // TODO:
        return null;
    }
}
