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
import io.supertokens.javalin.Constants;
import io.supertokens.javalin.ProcessState;
import io.supertokens.javalin.SuperTokens;
import io.supertokens.javalin.core.SessionFunctions;
import io.supertokens.javalin.core.exception.TokenTheftDetectedException;
import io.supertokens.javalin.core.exception.TryRefreshTokenException;
import io.supertokens.javalin.core.exception.UnauthorisedException;
import io.supertokens.javalin.core.informationHolders.SessionTokens;
import io.supertokens.javalin.core.querier.HttpRequestMocking;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class SessionTest {

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
    public void tokenTheftDetection() throws Exception {
        Utils.startST();
        SuperTokens.config("localhost:8080");

        SessionTokens response = SessionFunctions.createNewSession("", new JsonObject(), new JsonObject());

        SessionTokens response2 = SessionFunctions.refreshSession(response.refreshToken.token);

        SessionFunctions.getSession(response2.accessToken.token, response2.antiCsrfToken, true, response2.idRefreshToken.token);

        try {
            SessionFunctions.refreshSession(response.refreshToken.token);
            throw new Exception("should not have come here");
        } catch (TokenTheftDetectedException ignored) { }
    }

    @Test
    public void testBasicSessionUse() throws Exception {
        Utils.startST();
        SuperTokens.config("localhost:8080");

        SessionTokens response = SessionFunctions.createNewSession("", new JsonObject(), new JsonObject());
        assert (response.accessToken.token != null);
        assert (response.refreshToken.token != null);
        assert (response.idRefreshToken.token != null);
        assert (response.handle != null);
        assert (response.antiCsrfToken != null);

        SessionFunctions.getSession(response.accessToken.token, response.antiCsrfToken, true, response.idRefreshToken.token);
        assert (ProcessState.getInstance().getLastEventByName(ProcessState.PROCESS_STATE.CALLING_SERVICE_IN_VERIFY) == null);

        SessionTokens response2 = SessionFunctions.refreshSession(response.refreshToken.token);
        assert (response2.accessToken.token != null);
        assert (response2.refreshToken.token != null);
        assert (response2.idRefreshToken.token != null);
        assert (response2.handle != null);
        assert (response2.antiCsrfToken != null);

        SessionTokens response3 = SessionFunctions.getSession(response2.accessToken.token, response2.antiCsrfToken, true, response2.idRefreshToken.token);
        assert (ProcessState.getInstance().getLastEventByName(ProcessState.PROCESS_STATE.CALLING_SERVICE_IN_VERIFY) != null);
        assert(response3.handle != null);
        assert (response3.accessToken != null);
        assert (response3.antiCsrfToken == null);
        assert (response3.refreshToken == null);
        assert (response3.idRefreshToken == null);

        ProcessState.reset();

        SessionTokens response4 = SessionFunctions.getSession(response3.accessToken.token, response2.antiCsrfToken, true,
                response.idRefreshToken.token);
        assert (ProcessState.getInstance().getLastEventByName(ProcessState.PROCESS_STATE.CALLING_SERVICE_IN_VERIFY) == null);
        assert (response4.handle != null);
        assert (response4.accessToken == null);
        assert (response4.antiCsrfToken == null);
        assert (response4.refreshToken == null);
        assert (response4.idRefreshToken == null);

        assert (SessionFunctions.revokeSession(response4.handle));
    }

    @Test
    public void testSessionVerifyWithAntiCSRF() throws Exception {
        Utils.startST();
        SuperTokens.config("localhost:8080");

        SessionTokens response = SessionFunctions.createNewSession("", new JsonObject(), new JsonObject());

        SessionFunctions.getSession(response.accessToken.token, response.antiCsrfToken, true, response.idRefreshToken.token);

        SessionFunctions.getSession(response.accessToken.token, response.antiCsrfToken, false, response.idRefreshToken.token);
    }

    @Test
    public void testSessionVerifyWithoutAntiCSRF() throws Exception {
        Utils.startST();
        SuperTokens.config("localhost:8080");

        SessionTokens response = SessionFunctions.createNewSession("", new JsonObject(), new JsonObject());

        SessionFunctions.getSession(response.accessToken.token, response.antiCsrfToken, false, response.idRefreshToken.token);

        try {
            SessionFunctions.getSession(response.accessToken.token, null, true, response.idRefreshToken.token);
            throw new Exception("should not come here");
        } catch (TryRefreshTokenException ignored) { }
    }

    @Test
    public void testRevokingOfSessions() throws Exception {
        Utils.startST();
        SuperTokens.config("localhost:8080");

        SessionFunctions.revokeAllSessionsForUser("someUniqueUserId");

        SessionTokens response = SessionFunctions.createNewSession("someUniqueUserId", new JsonObject(), new JsonObject());
        assert (SessionFunctions.revokeSession(response.handle));

        String[] res3 = SessionFunctions.getAllSessionHandlesForUser("someUniqueUserId");
        assert (res3.length == 0);

        SessionFunctions.createNewSession("someUniqueUserId", new JsonObject(), new JsonObject());
        SessionFunctions.createNewSession("someUniqueUserId", new JsonObject(), new JsonObject());

        assert (SessionFunctions.revokeAllSessionsForUser("someUniqueUserId").length == 2);

        res3 = SessionFunctions.getAllSessionHandlesForUser("someUniqueUserId");
        assert (res3.length == 0);

        assert (!SessionFunctions.revokeSession(""));

        assert (SessionFunctions.revokeAllSessionsForUser("random").length == 0);

    }

    @Test
    public void testManipulationOfSessionData() throws Exception {
        Utils.startST();
        SuperTokens.config("localhost:8080");

        SessionTokens response = SessionFunctions
                .createNewSession("someUniqueUserId", new JsonObject(), new JsonObject());

        {
            JsonObject newData = new JsonObject();
            newData.addProperty("key", "value");
            SessionFunctions.updateSessionData(response.handle, newData);
        }

        {
            JsonObject data = SessionFunctions.getSessionData(response.handle);
            assert (data.get("key").getAsString().equals("value"));
        }

        {
            JsonObject newData = new JsonObject();
            newData.addProperty("key", "value2");
            SessionFunctions.updateSessionData(response.handle, newData);
        }

        {
            JsonObject data = SessionFunctions.getSessionData(response.handle);
            assert (data.get("key").getAsString().equals("value2"));
        }

        try {
            JsonObject newData = new JsonObject();
            newData.addProperty("key", "value2");
            SessionFunctions.updateSessionData("random", newData);
        } catch (UnauthorisedException ignored) {}

    }

    @Test
    public void testManipulationOfJWTPayload() throws Exception {
        Utils.startST();
        SuperTokens.config("localhost:8080");

        SessionTokens response = SessionFunctions
                .createNewSession("someUniqueUserId", new JsonObject(), new JsonObject());

        {
            JsonObject newData = new JsonObject();
            newData.addProperty("key", "value");
            SessionFunctions.updateJWTPayload(response.handle, newData);
        }

        {
            JsonObject data = SessionFunctions.getJWTPayload(response.handle);
            assert (data.get("key").getAsString().equals("value"));
        }

        {
            JsonObject newData = new JsonObject();
            newData.addProperty("key", "value2");
            SessionFunctions.updateJWTPayload(response.handle, newData);
        }

        {
            JsonObject data = SessionFunctions.getJWTPayload(response.handle);
            assert (data.get("key").getAsString().equals("value2"));
        }

        try {
            JsonObject newData = new JsonObject();
            newData.addProperty("key", "value2");
            SessionFunctions.updateJWTPayload("random", newData);
        } catch (UnauthorisedException ignored) {}

    }

    @Test
    public void testNoAntiCSRFRequiredIfDisabledFromCore() throws Exception {
        Utils.setKeyValueInConfig("enable_anti_csrf", "false");
        Utils.startST();
        SuperTokens.config("localhost:8080");

        SessionTokens response = SessionFunctions.createNewSession("", new JsonObject(), new JsonObject());

        SessionFunctions.getSession(response.accessToken.token, null, false, response.idRefreshToken.token);

        SessionFunctions.getSession(response.accessToken.token, null, true, response.idRefreshToken.token);
    }

}
