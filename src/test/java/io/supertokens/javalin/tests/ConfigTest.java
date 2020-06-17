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

import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.supertokens.javalin.Constants;
import io.supertokens.javalin.ProcessState;
import io.supertokens.javalin.SuperTokens;
import io.supertokens.javalin.core.HandshakeInfo;
import io.supertokens.javalin.core.SessionFunctions;
import io.supertokens.javalin.core.informationHolders.SessionTokens;
import io.supertokens.javalin.core.querier.HttpRequestMocking;
import io.supertokens.javalin.tests.httprequest.HttpRequest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

public class ConfigTest {

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
    public void testConfigHostURLWithTrailingSlash() throws Exception {
        Utils.startST();
        SuperTokens.config()
                .withHosts("http://localhost:8080/");
        SessionTokens response = SessionFunctions.createNewSession("", new JsonObject(), new JsonObject());
        assert (response.accessToken.token != null);
        assert (response.refreshToken.token != null);
        assert (response.idRefreshToken.token != null);
        assert (response.handle != null);
        assert (response.antiCsrfToken != null);

        SessionFunctions.getSession(response.accessToken.token, response.antiCsrfToken, true);
        assert (ProcessState.getInstance().getLastEventByName(ProcessState.PROCESS_STATE.CALLING_SERVICE_IN_VERIFY) == null);

        SessionTokens response2 = SessionFunctions.refreshSession(response.refreshToken.token);
        assert (response2.accessToken.token != null);
        assert (response2.refreshToken.token != null);
        assert (response2.idRefreshToken.token != null);
        assert (response2.handle != null);
        assert (response2.antiCsrfToken != null);

        SessionTokens response3 = SessionFunctions.getSession(response2.accessToken.token, response2.antiCsrfToken, true);
        assert (ProcessState.getInstance().getLastEventByName(ProcessState.PROCESS_STATE.CALLING_SERVICE_IN_VERIFY) != null);
        assert(response3.handle != null);
        assert (response3.accessToken != null);
        assert (response3.antiCsrfToken == null);
        assert (response3.refreshToken == null);
        assert (response3.idRefreshToken == null);

        ProcessState.reset();

        SessionTokens response4 = SessionFunctions.getSession(response3.accessToken.token, response2.antiCsrfToken, true);
        assert (ProcessState.getInstance().getLastEventByName(ProcessState.PROCESS_STATE.CALLING_SERVICE_IN_VERIFY) == null);
        assert (response4.handle != null);
        assert (response4.accessToken == null);
        assert (response4.antiCsrfToken == null);
        assert (response4.refreshToken == null);
        assert (response4.idRefreshToken == null);

        assert (SessionFunctions.revokeSession(response4.handle));

    }

    @Test
    public void testConfigPathsAreSet() throws Exception{
        Utils.startST();
        SuperTokens.config()
                .withHosts("http://localhost:8080")
                .withAccessTokenPath("/customAccessTokenPath")
                .withRefreshApiPath("/customRefreshPath")
                .withCookieDomain("custom.domain");

        Javalin app = null;
        try{
            app = Javalin.create().start("localhost", 8081);

            app.post("/create", ctx -> {
                SuperTokens.newSession(ctx, "").create();
                ctx.result("");
            });

            Map<String, String> response = Utils.extractInfoFromResponse(HttpRequest.sendJsonPOSTRequest("http://localhost:8081/create",
                    new JsonObject(), null));

            assert (response.get("accessTokenPath").equals("/customAccessTokenPath"));
            assert (response.get("refreshTokenPath").equals("/customRefreshPath"));
            assert (response.get("accessTokenDomain").equals("custom.domain"));
            assert (response.get("refreshTokenDomain").equals("custom.domain"));
        } finally {
            if (app != null) {
                app.stop();
            }
        }
    }
}
