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

package io.supertokens.javalin.core.accessToken;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.supertokens.javalin.core.Utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public class JWT {
    private static String HEADERv2 = null;

    private static void initHeader() {
        if (HEADERv2 == null) {
            JsonObject header = new JsonObject();
            header.addProperty("alg", "RS256");
            header.addProperty("typ", "JWT");
            header.addProperty("version", "2");
            JWT.HEADERv2 = Utils.convertToBase64(header.toString());
        }
    }

    public static JsonObject verifyJWTAndGetPayload(String jwt, String publicSigningKey)
            throws InvalidKeyException, NoSuchAlgorithmException, JWTException {
        initHeader();
        String[] splittedInput = jwt.split("\\.");
        if (splittedInput.length != 3) {
            throw new JWTException("Invalid JWT");
        }
        // checking header
        if (!splittedInput[0].equals(JWT.HEADERv2)) {
            throw new JWTException("JWT header mismatch");
        }
        // verifying signature
        String payload = splittedInput[1];
        try {
            if (!Utils.verifyWithPublicKey(splittedInput[0] + "." + payload, splittedInput[2], publicSigningKey)) {
                throw new JWTException("JWT verification failed");
            }
        } catch (InvalidKeySpecException | SignatureException e) {
            throw new JWTException("JWT verification failed");
        }
        return (JsonObject) new JsonParser().parse(Utils.convertFromBase64(splittedInput[1]));
    }

    public static class JWTException extends Exception {

        private static final long serialVersionUID = 1L;

        JWTException(String err) {
            super(err);
        }
    }
}
