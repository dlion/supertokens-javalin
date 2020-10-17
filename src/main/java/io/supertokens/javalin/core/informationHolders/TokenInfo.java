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

import org.jetbrains.annotations.Nullable;

public class TokenInfo {
    public final String token;
    public final long expiry;
    public final long createdTime;
    public final String cookiePath;
    public final boolean cookieSecure;
    @Nullable
    public final String domain;
    public final String sameSite;

    public TokenInfo(String token, long expiry, long createdTime,
                     String cookiePath, boolean cookieSecure, @Nullable String domain, String sameSite) {
        this.token = token;
        this.expiry = expiry;
        this.createdTime = createdTime;
        this.cookiePath = cookiePath;
        this.cookieSecure = cookieSecure;
        this.domain = domain;
        this.sameSite = sameSite;
    }
}
