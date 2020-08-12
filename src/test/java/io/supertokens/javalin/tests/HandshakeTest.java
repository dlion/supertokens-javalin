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

package io.supertokens.javalin.tests;

import io.supertokens.javalin.Constants;
import io.supertokens.javalin.ProcessState;
import io.supertokens.javalin.SuperTokens;
import io.supertokens.javalin.core.HandshakeInfo;
import io.supertokens.javalin.core.querier.HttpRequestMocking;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class HandshakeTest {

    @AfterClass
    public static void afterTesting() throws IOException, InterruptedException {
        Utils.killAllST();
        Utils.cleanST();
    }

    @Before
    public void beforeEach() throws IOException, InterruptedException {
        Utils.killAllST();
        Utils.setupST();
        ProcessState.reset();
        HttpRequestMocking.reset();
        Constants.IS_TESTING = true;
    }

    @Test
    public void driverInfoCheckWithoutFrontendSDK() throws Exception {
        Utils.startST();
        SuperTokens.config()
                .withHosts("http://localhost:8080");

        HandshakeInfo info = HandshakeInfo.getInstance();
        assert(info.accessTokenPath.equals("/"));
        assert(info.cookieDomain.equals("supertokens.io") || info.cookieDomain.equals("localhost") );
        assert(!info.cookieSecure);
        assert(info.refreshTokenPath.equals("/refresh") || info.refreshTokenPath.equals("/session/refresh"));
        assert(info.enableAntiCsrf);
        assert(!info.accessTokenBlacklistingEnabled);
        info.updateJwtSigningPublicKeyInfo("hello", 100);
        HandshakeInfo info2 = HandshakeInfo.getInstance();
        assert(info2.jwtSigningPublicKey.equals("hello"));
        assert(info2.jwtSigningPublicKeyExpiryTime == 100);

    }
}
