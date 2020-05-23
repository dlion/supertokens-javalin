package io.supertokens.javalin.core;

import com.google.gson.JsonObject;
import io.supertokens.javalin.core.Exception.GeneralException;
import io.supertokens.javalin.core.Exception.TokenTheftDetectedException;
import io.supertokens.javalin.core.Exception.TryRefreshTokenException;
import io.supertokens.javalin.core.Exception.UnauthorisedException;
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

    public static SessionTokens getSession(String accessToken, String antiCsrfToken, boolean doAntiCsrfCheck, String idRefreshToken) throws
            UnauthorisedException, TryRefreshTokenException {
        // TODO:
        return null;
    }

    public static SessionTokens refreshSession(String refreshToken) throws UnauthorisedException,
            TokenTheftDetectedException {
        // TODO:
        return null;
    }

    public static String[] revokeAllSessionsForUser(@NotNull String userId) throws GeneralException {
        // TODO:
        return null;
    }

    public static String[] getAllSessionHandlesForUser(@NotNull String userId) throws GeneralException {
        // TODO:
        return new String[]{};
    }

    public static boolean revokeSession(@NotNull String sessionHandle) throws GeneralException {
        // TODO:
        return false;
    }

    public static String[] revokeMultipleSessions(@NotNull String[] sessionHandles) throws GeneralException {
        // TODO:
        return new String[]{};
    }

    public static JsonObject getSessionData(@NotNull String sessionHandle) throws GeneralException, UnauthorisedException {
        // TODO:
        return null;
    }

    public static void updateSessionData(@NotNull String sessionHandle, @NotNull JsonObject sessionData) throws GeneralException, UnauthorisedException {
        // TODO:
    }

    public static JsonObject getJWTPayload(@NotNull String sessionHandle) throws GeneralException, UnauthorisedException {
        // TODO:
        return null;
    }

    public static void updateJWTPayload(@NotNull String sessionHandle, @NotNull JsonObject newJWTPayload) throws GeneralException, UnauthorisedException {
        // TODO:
    }

    public static SessionTokens regenerateSession(String accessToken, JsonObject userDataInJWT) throws GeneralException, UnauthorisedException {
        // TODO:
        return null;
    }
}


//---------------------------------------------

/*
*
* public void someTest() throws JsonProcessingException {
        SomeClass[] sc = new SomeClass[]{new SomeClass(), new SomeClass()};
        Map<String, Object> payloadClaims = new HashMap<>();
        payloadClaims.put("hi", sc);
        Map<String, Object> payloadClaims1 = new HashMap<>();
        payloadClaims1.put("one", payloadClaims);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        String payloadJson = mapper.writeValueAsString(payloadClaims1);
        System.out.println(payloadJson);
        System.out.println("------------");
        Map<String, Object> decoded = mapper.readValue(payloadJson, Map.class);
        Map a = (Map<String, Object>)decoded.get("one");
        ((SomeClass[])(a.get("hi")))[0].print();
    }

    private static class SomeClass {
        private int num = 123;

        public void print() {
            System.out.println(this.num);
        }
    }
* */
