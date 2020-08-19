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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.supertokens.javalin.core.exception.GeneralException;
import io.supertokens.javalin.core.informationHolders.SessionTokens;
import io.supertokens.javalin.core.informationHolders.TokenInfo;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class Utils {

    public static SessionTokens parseJsonResponse(JsonObject response) {
        JsonObject sessionJson = response.getAsJsonObject("session");
        JsonObject accessTokenJson = response.getAsJsonObject("accessToken");
        JsonObject refreshTokenJson = response.getAsJsonObject("refreshToken");
        JsonObject idRefreshTokenJson = response.getAsJsonObject("idRefreshToken");
        String antiCSRFToken = response.get("antiCsrfToken") == null ? null :
                response.get("antiCsrfToken").getAsString();

        TokenInfo accessToken = null;
        if (accessTokenJson != null) {
            accessToken = new TokenInfo(
                    accessTokenJson.get("token").getAsString(),
                    accessTokenJson.get("expiry").getAsLong(),
                    accessTokenJson.get("createdTime").getAsLong(),
                    accessTokenJson.get("cookiePath").getAsString(),
                    accessTokenJson.get("cookieSecure").getAsBoolean(),
                    accessTokenJson.has("domain") ? accessTokenJson.get("domain").getAsString() : null,
                    accessTokenJson.get("sameSite").getAsString());
        }
        TokenInfo refreshToken = null;
        if (refreshTokenJson != null) {
            refreshToken = new TokenInfo(
                    refreshTokenJson.get("token").getAsString(),
                    refreshTokenJson.get("expiry").getAsLong(),
                    refreshTokenJson.get("createdTime").getAsLong(),
                    refreshTokenJson.get("cookiePath").getAsString(),
                    refreshTokenJson.get("cookieSecure").getAsBoolean(),
                    refreshTokenJson.has("domain") ? refreshTokenJson.get("domain").getAsString() : null,
                    refreshTokenJson.get("sameSite").getAsString());
        }
        TokenInfo idRefreshToken = null;
        if (idRefreshTokenJson != null) {
            idRefreshToken = new TokenInfo(
                    idRefreshTokenJson.get("token").getAsString(),
                    idRefreshTokenJson.get("expiry").getAsLong(),
                    idRefreshTokenJson.get("createdTime").getAsLong(),
                    idRefreshTokenJson.get("cookiePath").getAsString(),
                    idRefreshTokenJson.get("cookieSecure").getAsBoolean(),
                    idRefreshTokenJson.has("domain") ? idRefreshTokenJson.get("domain").getAsString() : null,
                    idRefreshTokenJson.get("sameSite").getAsString());
        }
        return new SessionTokens(
                sessionJson.get("handle").getAsString(),
                sessionJson.get("userId").getAsString(),
                sessionJson.getAsJsonObject("userDataInJWT"),
                accessToken, refreshToken, idRefreshToken, antiCSRFToken);
    }

    public static String getLargestVersionFromIntersection(String[] v1, String[] v2) {
        Set<String> v2Set = new HashSet<>(Arrays.asList(v2));
        List<String> intesection = new ArrayList<>();
        for (String s : v1) {
            if (v2Set.contains(s)) {
                intesection.add(s);
            }
        }
        if (intesection.size() == 0) {
            return null;
        }
        String maxSoFar = intesection.get(0);
        for (String s : intesection) {
            maxSoFar = maxVersion(s, maxSoFar);
        }
        return maxSoFar;
    }

    public static String maxVersion(String version1, String version2) {
        String[] splittedV1 = version1.split("\\.");
        String[] splittedV2 = version2.split("\\.");
        int minLength = Math.min(splittedV1.length, splittedV2.length);
        for (int i = 0; i < minLength; i++) {
            int v1 = Integer.parseInt(splittedV1[i]);
            int v2 = Integer.parseInt(splittedV2[i]);
            if (v1 > v2) {
                return version1;
            } else if (v2 > v1) {
                return version2;
            }
        }
        if (splittedV1.length >= splittedV2.length) {
            return version1;
        }
        return version2;
    }

    public static String convertToBase64(String str) {
        return new String(Base64.getEncoder().encode(stringToBytes(str)));
    }

    private static byte[] stringToBytes(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }

    public static boolean verifyWithPublicKey(String content, String signature, String publicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        Signature sign = Signature.getInstance("SHA256withRSA");
        Base64.Decoder decoder = Base64.getDecoder();
        X509EncodedKeySpec ks = new X509EncodedKeySpec(decoder.decode(publicKey));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pub = kf.generatePublic(ks);

        sign.initVerify(pub);
        sign.update(stringToBytes(content));
        return sign.verify(decoder.decode(signature));
    }
    public static String convertFromBase64(String str) {
        return new String(Base64.getDecoder().decode(stringToBytes(str)));
    }

    public static Map<String, Object> jsonObjectToMap(JsonObject json) throws GeneralException {
        try {
            return new Gson().fromJson(json.toString(), new TypeToken<Map<String, Object>>(){}.getType());
        } catch (Exception e) {
            throw new GeneralException(e);
        }
    }

    public static JsonObject mapToJsonObject(Map<String, Object>  map) throws GeneralException {
        try {
            return new Gson().toJsonTree(map).getAsJsonObject();
        } catch (Exception e) {
            throw new GeneralException(e);
        }
    }

}
