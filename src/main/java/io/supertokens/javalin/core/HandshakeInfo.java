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

import com.google.gson.JsonObject;
import io.supertokens.javalin.core.exception.GeneralException;
import io.supertokens.javalin.core.querier.Querier;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

public class HandshakeInfo {

    private static HandshakeInfo instance = null;

    public  String jwtSigningPublicKey;
    @Nullable
    public final String cookieDomain;
    public final boolean cookieSecure;
    public final String accessTokenPath;
    public final String refreshTokenPath;
    public final boolean enableAntiCsrf;
    public final boolean accessTokenBlacklistingEnabled;
    public  long jwtSigningPublicKeyExpiryTime;
    public final String cookieSameSite;
    public final String idRefreshTokenPath;
    public final int sessionExpiredStatusCode;

    @TestOnly
    public static void reset() {
        instance = null;
    }

    public static HandshakeInfo getInstance() throws GeneralException {
        if (HandshakeInfo.instance == null) {
            synchronized (HandshakeInfo.class) {
                if (instance == null) {
                    JsonObject response = Querier.getInstance().sendPostRequest("handshake", "/handshake", new JsonObject());
                    HandshakeInfo.instance = new HandshakeInfo(
                            response.get("jwtSigningPublicKey").getAsString(),
                            response.has("cookieDomain") ? response.get("cookieDomain").getAsString(): null,
                            response.get("cookieSecure").getAsBoolean(),
                            response.get("accessTokenPath").getAsString(),
                            response.get("refreshTokenPath").getAsString(),
                            response.get("enableAntiCsrf").getAsBoolean(),
                            response.get("accessTokenBlacklistingEnabled").getAsBoolean(),
                            response.get("jwtSigningPublicKeyExpiryTime").getAsLong(),
                            response.get("cookieSameSite").getAsString(),
                            response.get("idRefreshTokenPath").getAsString(),
                            response.get("sessionExpiredStatusCode").getAsInt());
                }
            }
        }
        return HandshakeInfo.instance;
    }

    private HandshakeInfo(
            String jwtSigningPublicKey,
            @Nullable  String cookieDomain,
            boolean cookieSecure,
            String accessTokenPath,
            String refreshTokenPath,
            boolean enableAntiCsrf,
            boolean accessTokenBlacklistingEnabled,
            long jwtSigningPublicKeyExpiryTime,
            String cookieSameSite,
            String idRefreshTokenPath,
            int sessionExpiredStatusCode
    ) {
        this.jwtSigningPublicKey = jwtSigningPublicKey;
        this.cookieDomain = cookieDomain;
        this.cookieSecure = cookieSecure;
        this.accessTokenPath = accessTokenPath;
        this.refreshTokenPath = refreshTokenPath;
        this.enableAntiCsrf = enableAntiCsrf;
        this.accessTokenBlacklistingEnabled = accessTokenBlacklistingEnabled;
        this.jwtSigningPublicKeyExpiryTime = jwtSigningPublicKeyExpiryTime;
        this.cookieSameSite = cookieSameSite;
        this.idRefreshTokenPath = idRefreshTokenPath;
        this.sessionExpiredStatusCode = sessionExpiredStatusCode;
    }

    public void updateJwtSigningPublicKeyInfo(String newKey, long newExpiry) {
        synchronized (HandshakeInfo.class) {
            this.jwtSigningPublicKey = newKey;
            this.jwtSigningPublicKeyExpiryTime = newExpiry;
        }
    };
}
