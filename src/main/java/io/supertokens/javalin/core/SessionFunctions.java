/*
 * Copyright (c) 2020, VRAI Labs and/or its affiliates. All rights reserved.
 *
 * This software is licensed under the Apache License, Version 2.0 (the
 * "License") as published by the Apache Software Foundation.
 *
 * You may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.supertokens.javalin.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.supertokens.javalin.ProcessState;
import io.supertokens.javalin.core.accessToken.AccessToken;
import io.supertokens.javalin.core.exception.GeneralException;
import io.supertokens.javalin.core.exception.TokenTheftDetectedException;
import io.supertokens.javalin.core.exception.TryRefreshTokenException;
import io.supertokens.javalin.core.exception.UnauthorisedException;
import io.supertokens.javalin.core.informationHolders.SessionTokens;
import io.supertokens.javalin.core.querier.Querier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/*This can be moved to a separate package and be shared with other java webservers.*/

public class SessionFunctions {

    public static void config(String config, String apiKey) {
        Querier.initInstance(config, apiKey);
    }

    public static SessionTokens createNewSession(@NotNull String userId, @NotNull JsonObject jwtPayload,
                                                 @NotNull JsonObject sessionData) throws GeneralException {
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);
        body.add("userDataInJWT", jwtPayload);
        body.add("userDataInDatabase", sessionData);
        JsonObject response = Querier.getInstance().sendPostRequest("newsession", "/session", body);
        HandshakeInfo.getInstance().updateJwtSigningPublicKeyInfo(
                response.get("jwtSigningPublicKey").getAsString(),
                response.get("jwtSigningPublicKeyExpiryTime").getAsLong());
        return Utils.parseJsonResponse(response);
    }

    public static SessionTokens getSession(String accessToken, String antiCsrfToken, boolean doAntiCsrfCheck) throws
            UnauthorisedException, TryRefreshTokenException, GeneralException {

        // try to verify within SDK
        HandshakeInfo handshakeInfo = HandshakeInfo.getInstance();
        try {
            if (handshakeInfo.jwtSigningPublicKeyExpiryTime > System.currentTimeMillis()) {
                AccessToken.AccessTokenInfo accessTokenInfo = AccessToken.getInfoFromAccessToken(accessToken, handshakeInfo.jwtSigningPublicKey,
                            handshakeInfo.enableAntiCsrf && doAntiCsrfCheck);
                if (
                        handshakeInfo.enableAntiCsrf &&
                                doAntiCsrfCheck &&
                                (antiCsrfToken == null || !antiCsrfToken.equals(accessTokenInfo.antiCsrfToken))
                ) {
                    if (antiCsrfToken == null) {
                        throw new TryRefreshTokenException("provided antiCsrfToken is null. If you do not want anti-csrf check for this API, please set doAntiCsrfCheck to true");
                    } else {
                        throw new TryRefreshTokenException("anti-csrf check failed");
                    }
                }
                if (!handshakeInfo.accessTokenBlacklistingEnabled && accessTokenInfo.parentRefreshTokenHash1 == null) {
                    return new SessionTokens(accessTokenInfo.sessionHandle, accessTokenInfo.userId,
                            accessTokenInfo.userData, null, null, null, null);
                }
            }
        } catch (TryRefreshTokenException ignored) {}

        ProcessState.getInstance().addState(ProcessState.PROCESS_STATE.CALLING_SERVICE_IN_VERIFY, null);

        // send request below.

        JsonObject body = new JsonObject();
        body.addProperty("accessToken", accessToken);
        body.addProperty("doAntiCsrfCheck", doAntiCsrfCheck);
        if (antiCsrfToken != null) {
            body.addProperty("antiCsrfToken", antiCsrfToken);
        }
        JsonObject response = Querier.getInstance().sendPostRequest("getsession" ,"/session/verify", body);
        if (response.get("status").getAsString().equals("OK")) {
            return Utils.parseJsonResponse(response);
        } else if (response.get("status").getAsString().equals("UNAUTHORISED")) {
            throw new UnauthorisedException(response.get("message").getAsString());
        } else {
            throw new TryRefreshTokenException(response.get("message").getAsString());
        }
    }

    public static SessionTokens refreshSession(String refreshToken, @Nullable  String antiCsrfToken) throws UnauthorisedException,
            TokenTheftDetectedException, GeneralException {
        JsonObject body = new JsonObject();
        body.addProperty("refreshToken", refreshToken);
        if (antiCsrfToken != null) {
            body.addProperty("antiCsrfToken", antiCsrfToken);
        }
        JsonObject response = Querier.getInstance().sendPostRequest("refresh", "/session/refresh", body);
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
        JsonObject response = Querier.getInstance().sendPostRequest("revokeallsession", "/session/remove", body);
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
        JsonObject response = Querier.getInstance().sendGetRequest("getallsession", "/session/user", params);
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
        JsonObject response = Querier.getInstance().sendPostRequest("revokeMany", "/session/remove", body);
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
        JsonObject response = Querier.getInstance().sendGetRequest("getsessiondata", "/session/data", params);
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
        JsonObject response = Querier.getInstance().sendPutRequest("updatesessiondata", "/session/data", body);
        if (response.get("status").getAsString().equals("UNAUTHORISED")) {
            throw new UnauthorisedException(response.get("message").getAsString());
        }
    }

    public static JsonObject getJWTPayload(@NotNull String sessionHandle) throws GeneralException, UnauthorisedException {
        HashMap<String, String> params = new HashMap<>();
        params.put("sessionHandle", sessionHandle);
        JsonObject response = Querier.getInstance().sendGetRequest("getjwtpayload", "/jwt/data", params);
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
        JsonObject response = Querier.getInstance().sendPutRequest("updatejwtpayload", "/jwt/data", body);
        if (response.get("status").getAsString().equals("UNAUTHORISED")) {
            throw new UnauthorisedException(response.get("message").getAsString());
        }
    }

    public static SessionTokens regenerateSession(String accessToken, JsonObject newJWTPayload) throws GeneralException, UnauthorisedException {
        JsonObject body = new JsonObject();
        body.addProperty("accessToken", accessToken);
        body.add("userDataInJWT", newJWTPayload);
        JsonObject response = Querier.getInstance().sendPostRequest("regeneratesession", "/session/regenerate", body);
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
        Map<String, Object> decoded = mapper.readValue(payloadJson, Map.class);
        Map a = (Map<String, Object>)decoded.get("one");
        ((SomeClass[])(a.get("hi")))[0].print();
    }

    private static class SomeClass {
        private int num = 123;

        public void print() {
        }
    }
* */
