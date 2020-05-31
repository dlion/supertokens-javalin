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

package io.supertokens.javalin;

import io.javalin.http.Context;
import io.supertokens.javalin.core.exception.GeneralException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SessionBuilder {

    @NotNull
    private final Context ctx;

    @NotNull
    private final String userId;

    @NotNull
    private Map<String, Object> jwtPayload = new HashMap<>();

    @NotNull
    private Map<String, Object>  sessionData = new HashMap<>();

    SessionBuilder(@NotNull Context ctx, @NotNull String userId) {
        this.ctx = ctx;
        this.userId = userId;
    }

    public void withJWTPayload(@NotNull Map<String, Object>  jwtPayload) {
        this.jwtPayload = jwtPayload;
    }

    public void withSessionData(@NotNull Map<String, Object>  sessionData) {
        this.sessionData = sessionData;
    }

    public Session create() throws GeneralException {
        return SuperTokens.createNewSession(this.ctx, this.userId, this.jwtPayload, this.sessionData);
    }
}
