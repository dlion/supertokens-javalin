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
import io.supertokens.javalin.Session;
import io.supertokens.javalin.SuperTokens;
import io.supertokens.javalin.core.exception.SuperTokensException;
import io.supertokens.javalin.core.querier.HttpRequestMocking;
import io.supertokens.javalin.tests.httprequest.HttpRequest;
import net.bytebuddy.implementation.bind.annotation.Super;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MiddlewareTest {

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
    public void testMiddlewareWithTrySupertokensURL() throws Exception{
        Utils.startST("localhost",3567);
        SuperTokens.config()
                .withHosts("https://try.supertokens.io");

        Javalin app = null;
        try{
            app = Javalin.create().start("localhost", 8081);

            app.post("/create", ctx -> {
                SuperTokens.newSession(ctx, "testing-userId").create();
                ctx.result("");
            });

            app.before("/verify", SuperTokens.middleware());
            app.get("/verify", ctx -> {
                SuperTokens.getSession(ctx,true);
                ctx.result("{\"message\": true}");
            });

            app.before("/refresh", SuperTokens.middleware());
            app.post("/refresh", ctx -> {
                ctx.result("{\"message\": true}");
            });

            app.before("/logout", SuperTokens.middleware());
            app.post("/logout", ctx -> {
                Session session = SuperTokens.getFromContext(ctx);
                session.revokeSession();
                ctx.result("{\"message\": true}");
            });

            Map<String, String> response = Utils.extractInfoFromResponse(HttpRequest.sendJsonPOSTRequest("http://localhost:8081/create",
                    new JsonObject(), null));
            assert (response.get("accessToken") != null);
            assert (response.get("antiCsrf") != null);
            assert (response.get("idRefreshTokenFromCookie") != null);
            assert (response.get("idRefreshTokenFromHeader") != null);
            assert (response.get("refreshToken") != null);

            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response.get("accessToken") + ";sIdRefreshToken=" +
                        response.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", response.get("antiCsrf"));
                HttpRequest.sendJsonPOSTRequest("http://localhost:8081/verify", new JsonObject(), headers);
            }

            Map<String, String> response2;
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sRefreshToken=" + response.get("refreshToken"));
                HttpURLConnection con = HttpRequest.sendJsonPOSTRequest("http://localhost:8081/refresh",
                        new JsonObject(), headers);
                response2 = Utils.extractInfoFromResponse(con);
            }
            assert (response2.get("accessToken") != null);
            assert (response2.get("antiCsrf") != null);
            assert (response2.get("idRefreshTokenFromCookie") != null);
            assert (response2.get("idRefreshTokenFromHeader") != null);
            assert (response2.get("refreshToken") != null);

            Map<String, String> response3;
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response2.get("accessToken") + ";sIdRefreshToken=" +
                        response2.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", response2.get("antiCsrf"));
                response3 = Utils.extractInfoFromResponse(
                        HttpRequest.sendJsonPOSTRequest("http://localhost:8081/verify", new JsonObject(), headers));
                assert (ProcessState.getInstance().getLastEventByName(ProcessState.PROCESS_STATE.CALLING_SERVICE_IN_VERIFY) != null);
            }

            ProcessState.reset();

            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response3.get("accessToken") + ";sIdRefreshToken=" +
                        response3.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", response2.get("antiCsrf"));
                Utils.extractInfoFromResponse(
                        HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/verify", new JsonObject(), headers));
            }

            Map<String, String> response4;
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response3.get("accessToken") + ";sIdRefreshToken=" +
                        response3.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", response2.get("antiCsrf"));
                response4 = Utils.extractInfoFromResponse(
                        HttpRequest.sendJsonPOSTRequest("http://localhost:8081/logout", new JsonObject(), headers));
            }

            assert(response4.get("antiCsrf") == null);
            assert(response4.get("accessToken").equals(""));
            assert(response4.get("refreshToken").equals(""));
            assert(response4.get("idRefreshTokenFromHeader").equals("remove"));
            assert(response4.get("idRefreshTokenFromCookie").equals(""));
            assert(response4.get("accessTokenExpiry").equals("Thu, 01-Jan-1970 00:00:00 GMT"));
            assert(response4.get("idRefreshTokenExpiry").equals("Thu, 01-Jan-1970 00:00:00 GMT"));
            assert(response4.get("refreshTokenExpiry").equals("Thu, 01-Jan-1970 00:00:00 GMT"));


        } finally {
            if (app != null) {
                app.stop();
            }
        }
    }

    @Test
    public void testMiddlware() throws Exception {
        Utils.setKeyValueInConfig("refresh_api_path", "/refresh");
        Utils.startST("localhost", 3567);

        Javalin app = null;
        try {
            app = Javalin.create().start("localhost", 8081);

            app.post("/create", ctx -> {
                SuperTokens.newSession(ctx, "testing-userId").create();
                ctx.result("");
            });

            app.before("/user/id", SuperTokens.middleware());
            app.get("/user/id", ctx -> {
                Session session = SuperTokens.getFromContext(ctx);
                ctx.result("{\"message\": " + session.getUserId() + "}");
            });

            app.before("/user/handle", SuperTokens.middleware(true));
            app.get("/user/handle", ctx -> {
                Session session = SuperTokens.getFromContext(ctx);
                ctx.result("{\"message\": " + session.getSessionHandle() + "}");
            });

            app.before("/refresh", SuperTokens.middleware());
            app.post("/refresh", ctx -> {
                ctx.result("{\"message\": true}");
            });

            app.before("/logout", SuperTokens.middleware());
            app.post("/logout", ctx -> {
                Session session = SuperTokens.getFromContext(ctx);
                session.revokeSession();
                ctx.result("{\"message\": true}");
            });

            app.exception(SuperTokensException.class, SuperTokens.exceptionHandler().onTryRefreshTokenError((exception, ctx) -> {
                ctx.status(401).result("{\"message\": try refresh token}");
            }).onTokenTheftDetectedError((exception, ctx) -> {
                ctx.status(403).result("{\"message\": token theft detected}");
            }).onGeneralError((exception, ctx) -> {
                ctx.status(400).result("{\"message\": general error}");
            }));

            app.exception(Exception.class, (e, ctx) -> {
                ctx.result("");
            });


            Map<String, String> response = Utils.extractInfoFromResponse(HttpRequest.sendJsonPOSTRequest("http://localhost:8081/create",
                    new JsonObject(), null));

            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response.get("accessToken") + ";sIdRefreshToken=" +
                        response.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", response.get("antiCsrf"));
                HttpURLConnection con = HttpRequest.sendGETRequest("http://localhost:8081/user/id", new HashMap<>(), headers);

                InputStream inputStream = con.getInputStream();
                StringBuilder responseStr = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        responseStr.append(inputLine);
                    }
                }
                assert (responseStr.toString().equals("{\"message\": testing-userId}"));
            }

            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response.get("accessToken") + ";sIdRefreshToken=" +
                        response.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", response.get("antiCsrf"));
                HttpURLConnection con = HttpRequest.sendGETRequest("http://localhost:8081/user/handle", new HashMap<>(), headers);

                assert (con.getResponseCode() == 200);
            }

            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response.get("accessToken") + ";sIdRefreshToken=" +
                        response.get("idRefreshTokenFromCookie"));
                HttpURLConnection con = HttpRequest.sendGETRequest("http://localhost:8081/user/handle", new HashMap<>(), headers);

                assert (con.getResponseCode() == 401);
                InputStream inputStream = con.getErrorStream();
                StringBuilder responseStr = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        responseStr.append(inputLine);
                    }
                }
                assert (responseStr.toString().equals("{\"message\": try refresh token}"));
            }

            // no idRefreshToken
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response.get("accessToken"));
                headers.put("anti-csrf", response.get("antiCsrf"));
                HttpURLConnection con = HttpRequest.sendGETRequest("http://localhost:8081/user/handle", new HashMap<>(), headers);

                assert (con.getResponseCode() == 401 || con.getResponseCode() == 440);
                InputStream inputStream = con.getErrorStream();
                StringBuilder responseStr = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        responseStr.append(inputLine);
                    }
                }
                assert (responseStr.toString().equals("unauthorised"));
            }

            Map<String, String> h = new HashMap<>();
            h.put("Cookie", "sRefreshToken=" + response.get("refreshToken"));
            Map<String, String> response2 = Utils.extractInfoFromResponse(HttpRequest.sendJsonPOSTRequest("http://localhost:8081/refresh",
                    new JsonObject(), h));

            Map<String, String> response3;
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response2.get("accessToken") + ";sIdRefreshToken=" +
                        response2.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", response2.get("antiCsrf"));
                HttpURLConnection con = HttpRequest.sendGETRequest("http://localhost:8081/user/id", new HashMap<>(), headers);

                InputStream inputStream = con.getInputStream();
                StringBuilder responseStr = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        responseStr.append(inputLine);
                    }
                }
                assert (responseStr.toString().equals("{\"message\": testing-userId}"));
                response3 = Utils.extractInfoFromResponse(con);
            }

            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response3.get("accessToken") + ";sIdRefreshToken=" +
                        response2.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", response2.get("antiCsrf"));
                HttpURLConnection con = HttpRequest.sendGETRequest("http://localhost:8081/user/handle", new HashMap<>(), headers);

                assert (con.getResponseCode() == 200);
            }

            {
                Map<String, String> header = new HashMap<>();
                header.put("Cookie", "sRefreshToken=" + response.get("refreshToken"));
                HttpURLConnection con = HttpRequest.sendJsonPOSTRequest("http://localhost:8081/refresh", new JsonObject(), header);

                assert(con.getResponseCode() == 403);
                InputStream inputStream = con.getErrorStream();
                StringBuilder responseStr = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        responseStr.append(inputLine);
                    }
                }
                assert (responseStr.toString().equals("{\"message\": token theft detected}"));
            }

            Map<String, String> response4;
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response3.get("accessToken") + ";sIdRefreshToken=" +
                        response2.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", response2.get("antiCsrf"));
                HttpURLConnection con = HttpRequest.sendJsonPOSTRequest("http://localhost:8081/logout",
                        new JsonObject(), headers);

                assert(con.getResponseCode() == 200);
                response4 = Utils.extractInfoFromResponse(con);
                assert(response4.get("antiCsrf") == null);
                assert(response4.get("accessToken").equals(""));
                assert(response4.get("refreshToken").equals(""));
                assert(response4.get("idRefreshTokenFromHeader").equals("remove"));
                assert(response4.get("idRefreshTokenFromCookie").equals(""));
                assert(response4.get("accessTokenExpiry").equals("Thu, 01-Jan-1970 00:00:00 GMT"));
                assert(response4.get("idRefreshTokenExpiry").equals("Thu, 01-Jan-1970 00:00:00 GMT"));
                assert(response4.get("refreshTokenExpiry").equals("Thu, 01-Jan-1970 00:00:00 GMT"));

            }

            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response4.get("accessToken") + ";sIdRefreshToken=" +
                        response4.get("idRefreshTokenFromCookie"));
                HttpURLConnection con = HttpRequest.sendGETRequest("http://localhost:8081/user/handle", new HashMap<>(), headers);

                assert (con.getResponseCode() == 401);
            }
        } finally {
            if (app != null) {
                app.stop();
            }
        }
    }
}
