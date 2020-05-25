package io.supertokens.javalin.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.supertokens.javalin.core.Exception.GeneralException;
import io.supertokens.javalin.core.Exception.TokenTheftDetectedException;
import io.supertokens.javalin.core.Exception.TryRefreshTokenException;
import io.supertokens.javalin.core.Exception.UnauthorisedException;
import io.supertokens.javalin.core.InformationHolders.SessionTokens;
import io.supertokens.javalin.core.Querier.Querier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/*This can be moved to a separate package and be shared with other java webservers.*/

public class SessionFunctions {

    public static void config(String config) throws GeneralException {
        Querier.initInstance(config);
    }

    public static SessionTokens createNewSession(@NotNull String userId, @Nullable JsonObject jwtPayload,
                                                 @Nullable JsonObject sessionData) throws GeneralException {
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);
        body.add("userDataInJWT", jwtPayload);
        body.add("userDataInDatabase", sessionData);
        JsonObject response = Querier.getInstance().sendPostRequest("/session", body);
        HandshakeInfo.getInstance().updateJwtSigningPublicKeyInfo(
                response.get("jwtSigningPublicKey").getAsString(),
                response.get("jwtSigningPublicKeyExpiryTime").getAsLong());
        return Utils.parseJsonResponse(response);
    }

    public static SessionTokens getSession(String accessToken, String antiCsrfToken, boolean doAntiCsrfCheck, String idRefreshToken) throws
            UnauthorisedException, TryRefreshTokenException, GeneralException {
        // TODO:
        return null;
    }

    public static SessionTokens refreshSession(String refreshToken) throws UnauthorisedException,
            TokenTheftDetectedException, GeneralException {
        JsonObject body = new JsonObject();
        body.addProperty("refreshToken", refreshToken);
        JsonObject response = Querier.getInstance().sendPostRequest("/session/refresh", body);
        if (response.get("status").getAsString().equals("OK")) {
            return Utils.parseJsonResponse(response);
        } else if (response.get("status").getAsString().equals("UNAUTHORISED")) {
            throw new UnauthorisedException(response.get("message").getAsString());
        } else {
            throw new TokenTheftDetectedException(
                    response.get("session").getAsJsonObject().get("handle").getAsString(),
                    response.get("session").getAsJsonObject().get("userId").getAsString());
        }
    }

    public static String[] revokeAllSessionsForUser(@NotNull String userId) throws GeneralException {
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);
        JsonObject response = Querier.getInstance().sendPostRequest("/session/remove", body);
        JsonArray jsonArray = response.get("sessionHandlesRevoked").getAsJsonArray();

        String[] result = new String[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            result[i] = jsonArray.get(i).getAsString();
        }
        return result;
    }

    public static String[] getAllSessionHandlesForUser(@NotNull String userId) throws GeneralException {
        HashMap<String, String> params = new HashMap<>();
        params.put("userId", userId);
        JsonObject response = Querier.getInstance().sendGetRequest("/session/user", params);
        JsonArray jsonArray = response.get("sessionHandles").getAsJsonArray();

        String[] result = new String[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            result[i] = jsonArray.get(i).getAsString();
        }
        return result;
    }

    public static boolean revokeSession(@NotNull String sessionHandle) throws GeneralException {
        return revokeMultipleSessions(new String[]{sessionHandle}).length == 1;
    }

    public static String[] revokeMultipleSessions(@NotNull String[] sessionHandles) throws GeneralException {
        JsonArray sessionHandleJson = new JsonArray();
        for (String handle : sessionHandles) {
            sessionHandleJson.add(new JsonPrimitive(handle));
        }
        JsonObject body = new JsonObject();
        body.add("sessionHandles", sessionHandleJson);
        JsonObject response = Querier.getInstance().sendPostRequest("/session/remove", body);
        JsonArray jsonArray = response.get("sessionHandlesRevoked").getAsJsonArray();

        String[] result = new String[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            result[i] = jsonArray.get(i).getAsString();
        }
        return result;
    }

    public static JsonObject getSessionData(@NotNull String sessionHandle) throws GeneralException, UnauthorisedException {
        HashMap<String, String> params = new HashMap<>();
        params.put("sessionHandle", sessionHandle);
        JsonObject response = Querier.getInstance().sendGetRequest("/session/data", params);
        if (response.get("status").getAsString().equals("OK")) {
            return response.get("userDataInDatabase").getAsJsonObject();
        } else {
            throw new UnauthorisedException(response.get("message").getAsString());
        }
    }

    public static void updateSessionData(@NotNull String sessionHandle, @NotNull JsonObject newSessionData) throws GeneralException, UnauthorisedException {
        JsonObject body = new JsonObject();
        body.addProperty("sessionHandle", sessionHandle);
        body.add("userDataInDatabase", newSessionData);
        JsonObject response = Querier.getInstance().sendPutRequest("/session/data", body);
        if (response.get("status").getAsString().equals("UNAUTHORISED")) {
            throw new UnauthorisedException(response.get("message").getAsString());
        }
    }

    public static JsonObject getJWTPayload(@NotNull String sessionHandle) throws GeneralException, UnauthorisedException {
        HashMap<String, String> params = new HashMap<>();
        params.put("sessionHandle", sessionHandle);
        JsonObject response = Querier.getInstance().sendGetRequest("/jwt/data", params);
        if (response.get("status").getAsString().equals("OK")) {
            return response.get("userDataInJWT").getAsJsonObject();
        } else {
            throw new UnauthorisedException(response.get("message").getAsString());
        }
    }

    public static void updateJWTPayload(@NotNull String sessionHandle, @NotNull JsonObject newJWTPayload) throws GeneralException, UnauthorisedException {
        JsonObject body = new JsonObject();
        body.addProperty("sessionHandle", sessionHandle);
        body.add("userDataInJWT", newJWTPayload);
        JsonObject response = Querier.getInstance().sendPutRequest("/jwt/data", body);
        if (response.get("status").getAsString().equals("UNAUTHORISED")) {
            throw new UnauthorisedException(response.get("message").getAsString());
        }
    }

    public static SessionTokens regenerateSession(String accessToken, JsonObject newJWTPayload) throws GeneralException, UnauthorisedException {
        JsonObject body = new JsonObject();
        body.addProperty("accessToken", accessToken);
        body.add("userDataInJWT", newJWTPayload);
        JsonObject response = Querier.getInstance().sendPostRequest("/session/regenerate", body);
        if (response.get("status").getAsString().equals("UNAUTHORISED")) {
            throw new UnauthorisedException(response.get("message").getAsString());
        } else {
            return Utils.parseJsonResponse(response);
        }
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
