package io.supertokens.javalin;

import com.google.gson.JsonObject;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuperTokens {

    public Session createNewSession(@NotNull Context ctx, @NotNull  String userId,
                                    @Nullable JsonObject jwtPayload,
                                    @Nullable JsonObject sessionData) {
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

    public String[] revokeAllSessionsForUser(@NotNull String userId) {
        // TODO:
        return new String[]{};
    }

    public String[] getAllSessionHandlesForUser(@NotNull String userId) {
        // TODO:
        return new String[]{};
    }

    public boolean revokeSession(@NotNull String sessionHandle) {
        // TODO:
        return false;
    }

    public String[] revokeMultipleSessions(@NotNull String[] sessionHandle) {
        // TODO:
        return new String[]{};
    }

    public JsonObject getSessionData(@NotNull String sessionHandle) {
        // TODO:
        return null;
    }

    public void updateSessionData(@NotNull String sessionHandle, @NotNull JsonObject sessionData) {
        // TODO:
    }

    public void setRelevantHeadersForOptionsAPI(@NotNull Context ctx) {
        // TODO:
    }

    public JsonObject geJWTPayload(@NotNull String sessionHandle) {
        // TODO:
        return null;
    }

    public void updateJWTPayload(@NotNull String sessionHandle, @NotNull JsonObject newJWTPayload) {
        // TODO:
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
