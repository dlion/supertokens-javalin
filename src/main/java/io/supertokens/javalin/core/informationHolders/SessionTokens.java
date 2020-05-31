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

package io.supertokens.javalin.core.informationHolders;

import com.google.gson.JsonObject;

public class SessionTokens {
    public final String handle;
    public final String userId;
    public final JsonObject userDataInJWT;
    public final TokenInfo accessToken;
    public final TokenInfo refreshToken;
    public final TokenInfo idRefreshToken;
    public final String antiCsrfToken;

    public SessionTokens(String handle, String userId, JsonObject userDataInJWT, TokenInfo accessToken,
                         TokenInfo refreshToken, TokenInfo idRefreshToken, String antiCsrfToken) {
        this.handle = handle;
        this.userId = userId;
        this.userDataInJWT = userDataInJWT;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.idRefreshToken = idRefreshToken;
        this.antiCsrfToken = antiCsrfToken;
    }
}
