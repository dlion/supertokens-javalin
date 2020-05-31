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

package io.supertokens.javalin.core.exception;

public class TokenTheftDetectedException extends SuperTokensException {

    private final String sessionHandle;

    private final String userId;

    public TokenTheftDetectedException(String sessionHandle, String userId) {
        super("Token theft detected");
        this.sessionHandle = sessionHandle;
        this.userId = userId;
    }

    public String getSessionHandle() {
        return this.sessionHandle;
    }

    public String getUserId() {
        return this.userId;
    }

}
