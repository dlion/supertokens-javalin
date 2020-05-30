package io.supertokens.javalin.tests;

import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.supertokens.javalin.Constants;
import io.supertokens.javalin.ProcessState;
import io.supertokens.javalin.Session;
import io.supertokens.javalin.SuperTokens;
import io.supertokens.javalin.core.SessionFunctions;
import io.supertokens.javalin.core.exception.TokenTheftDetectedException;
import io.supertokens.javalin.core.querier.HttpRequestMocking;
import io.supertokens.javalin.tests.httprequest.HttpRequest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class SuperTokensTest {

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
        Javalin app = null;
        try {
            app = Javalin.create().start("localhost", 8081);

            app.post("/create", ctx -> {
                SuperTokens.newSession(ctx, "").create();
                ctx.result("");
            });

            app.post("/session/verify", ctx -> {
                SuperTokens.getSession(ctx, true);
                ctx.result("");
            });

            app.post("/session/refresh", ctx -> {
                try {
                    SuperTokens.refreshSession(ctx);
                    ctx.result("{\"success\": false}");
                } catch (TokenTheftDetectedException e) {
                    ctx.result("{\"success\": true}");
                } catch (Exception e) {
                    ctx.result("{\"success\": false}");
                }
            });

            Map<String, String> response = Utils.extractInfoFromResponse(HttpRequest.sendJsonPOSTRequest("http://localhost:8081/create",
                    new JsonObject(), null));

            Map<String, String> response2;
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sRefreshToken=" + response.get("refreshToken"));
                response2 = Utils
                        .extractInfoFromResponse(HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/refresh",
                                new JsonObject(), headers));
            }

            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response2.get("accessToken") + ";sIdRefreshToken=" +
                        response2.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", response2.get("antiCsrf"));
                HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/verify", new JsonObject(), headers);
            }

            Map<String, String> response3;
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sRefreshToken=" + response.get("refreshToken"));
                HttpURLConnection con = HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/refresh",
                        new JsonObject(), headers);
                InputStream inputStream = con.getInputStream();
                StringBuilder responseStr = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        responseStr.append(inputLine);
                    }
                }
                assert (responseStr.toString().equals("{\"success\": true}"));
                response3 = Utils
                        .extractInfoFromResponse(con);
            }

            assert(response3.get("antiCsrf") == null);
            assert(response3.get("accessToken").equals(""));
            assert(response3.get("refreshToken").equals(""));
            assert(response3.get("idRefreshTokenFromHeader").equals("remove"));
            assert(response3.get("idRefreshTokenFromCookie").equals(""));
            assert(response3.get("accessTokenExpiry").equals("Thu, 01-Jan-1970 00:00:00 GMT"));
            assert(response3.get("idRefreshTokenExpiry").equals("Thu, 01-Jan-1970 00:00:00 GMT"));
            assert(response3.get("refreshTokenExpiry").equals("Thu, 01-Jan-1970 00:00:00 GMT"));

        } finally {
            if (app != null) {
                app.stop();
            }
        }
    }

    @Test
    public void basicUsage() throws Exception {
        Utils.startST();
        SuperTokens.config("localhost:8080");
        Javalin app = null;
        try {
            app = Javalin.create().start("localhost", 8081);

            app.post("/create", ctx -> {
                SuperTokens.newSession(ctx, "").create();
                ctx.result("");
            });

            app.post("/session/verify", ctx -> {
                SuperTokens.getSession(ctx, true);
                ctx.result("");
            });

            app.post("/session/refresh", ctx -> {
                SuperTokens.refreshSession(ctx);
                ctx.result("");
            });

            app.post("/session/revoke", ctx -> {
                Session s = SuperTokens.getSession(ctx, true);
                s.revokeSession();
                ctx.result("");
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
                HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/verify", new JsonObject(), headers);
                assert (ProcessState.getInstance().getLastEventByName(ProcessState.PROCESS_STATE.CALLING_SERVICE_IN_VERIFY) == null);
            }

            Map<String, String> response2;
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sRefreshToken=" + response.get("refreshToken"));
                HttpURLConnection con = HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/refresh",
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
                        HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/verify", new JsonObject(), headers));
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
                assert (ProcessState.getInstance().getLastEventByName(ProcessState.PROCESS_STATE.CALLING_SERVICE_IN_VERIFY) == null);
            }

            Map<String, String> response4;
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response3.get("accessToken") + ";sIdRefreshToken=" +
                        response3.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", response2.get("antiCsrf"));
                response4 = Utils.extractInfoFromResponse(
                        HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/revoke", new JsonObject(), headers));
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
    public void sessionVerifyWithAntiCsrf() throws Exception {
        Utils.startST();
        SuperTokens.config("localhost:8080");
        Javalin app = null;
        try {
            app = Javalin.create().start("localhost", 8081);

            app.post("/create", ctx -> {
                SuperTokens.newSession(ctx, "id1").create();
                ctx.result("");
            });

            app.post("/session/verify", ctx -> {
                SuperTokens.getSession(ctx, true);
                ctx.result("");
            });

            app.post("/session/verifyAntiCsrfFalse", ctx -> {
                SuperTokens.getSession(ctx, false);
                ctx.result("");
            });

            Map<String, String> response = Utils.extractInfoFromResponse(HttpRequest.sendJsonPOSTRequest("http://localhost:8081/create",
                    new JsonObject(), null));

            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response.get("accessToken") + ";sIdRefreshToken=" +
                        response.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", response.get("antiCsrf"));
                HttpURLConnection con = HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/verify", new JsonObject(), headers);
                assert (con.getResponseCode() == 200);
            }

            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response.get("accessToken") + ";sIdRefreshToken=" +
                        response.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", response.get("antiCsrf"));
                HttpURLConnection con = HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/verifyAntiCsrfFalse", new JsonObject(), headers);
                assert (con.getResponseCode() == 200);
            }

        } finally {
            if (app != null) {
                app.stop();
            }
        }
    }


    @Test
    public void sessionVerifyWithoutAntiCsrf() throws Exception {
        Utils.startST();
        SuperTokens.config("localhost:8080");
        Javalin app = null;
        try {
            app = Javalin.create().start("localhost", 8081);

            app.post("/create", ctx -> {
                SuperTokens.newSession(ctx, "id1").create();
                ctx.result("");
            });

            app.post("/session/verify", ctx -> {
                SuperTokens.getSession(ctx, false);
                ctx.result("");
            });

            app.post("/session/verifyAntiCsrfFalse", ctx -> {
                SuperTokens.getSession(ctx, false);
                ctx.result("");
            });

            Map<String, String> response = Utils.extractInfoFromResponse(HttpRequest.sendJsonPOSTRequest("http://localhost:8081/create",
                    new JsonObject(), null));

            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response.get("accessToken") + ";sIdRefreshToken=" +
                        response.get("idRefreshTokenFromCookie"));
                HttpURLConnection con = HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/verify", new JsonObject(), headers);
                assert (con.getResponseCode() == 200);
            }

            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response.get("accessToken") + ";sIdRefreshToken=" +
                        response.get("idRefreshTokenFromCookie"));
                HttpURLConnection con = HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/verifyAntiCsrfFalse", new JsonObject(), headers);
                assert (con.getResponseCode() == 200);
            }

        } finally {
            if (app != null) {
                app.stop();
            }
        }
    }

    @Test
    public void sessionRevoking() throws Exception {
        Utils.startST();
        SuperTokens.config("localhost:8080");
        Javalin app = null;
        try {
            app = Javalin.create().start("localhost", 8081);

            app.post("/create", ctx -> {
                SuperTokens.newSession(ctx, "").create();
                ctx.result("");
            });

            app.post("/usercreate", ctx -> {
                SuperTokens.newSession(ctx, "someUniqueUserId").create();
                ctx.result("");
            });

            app.post("/session/revoke", ctx -> {
                Session s = SuperTokens.getSession(ctx, true);
                s.revokeSession();
                ctx.result("");
            });

            app.post("/session/revokeUserid", ctx -> {
                Session s = SuperTokens.getSession(ctx, true);
                SuperTokens.revokeAllSessionsForUser(s.getUserId());
                ctx.result("");
            });

            app.post("/session/getSessionsWithUserId1", ctx -> {
                String[] sessionHandles = SuperTokens.getAllSessionHandlesForUser("someUniqueUserId");
                ctx.result("" + sessionHandles.length);
            });

            Map<String, String> response = Utils.extractInfoFromResponse(HttpRequest.sendJsonPOSTRequest("http://localhost:8081/create",
                    new JsonObject(), null));

            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response.get("accessToken") + ";sIdRefreshToken=" +
                        response.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", response.get("antiCsrf"));
                Map<String, String> sessionRevokedResponse = Utils.extractInfoFromResponse(
                        HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/revoke", new JsonObject(), headers));

                assert(sessionRevokedResponse.get("antiCsrf") == null);
                assert(sessionRevokedResponse.get("accessToken").equals(""));
                assert(sessionRevokedResponse.get("refreshToken").equals(""));
                assert(sessionRevokedResponse.get("idRefreshTokenFromHeader").equals("remove"));
                assert(sessionRevokedResponse.get("idRefreshTokenFromCookie").equals(""));
                assert(sessionRevokedResponse.get("accessTokenExpiry").equals("Thu, 01-Jan-1970 00:00:00 GMT"));
                assert(sessionRevokedResponse.get("idRefreshTokenExpiry").equals("Thu, 01-Jan-1970 00:00:00 GMT"));
                assert(sessionRevokedResponse.get("refreshTokenExpiry").equals("Thu, 01-Jan-1970 00:00:00 GMT"));
            }

            HttpRequest.sendJsonPOSTRequest("http://localhost:8081/create", new JsonObject(), null);

            Map<String, String> userCreateResponse = Utils.extractInfoFromResponse(HttpRequest.sendJsonPOSTRequest("http://localhost:8081/usercreate",
                    new JsonObject(), null));
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + userCreateResponse.get("accessToken") + ";sIdRefreshToken=" +
                        userCreateResponse.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", userCreateResponse.get("antiCsrf"));
                HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/revokeUserid", new JsonObject(), headers);
            }

            {
                HttpURLConnection con = HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/getSessionsWithUserId1", new JsonObject(), null);
                InputStream inputStream = con.getInputStream();
                StringBuilder responseStr = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        responseStr.append(inputLine);
                    }
                }
                assert (responseStr.toString().equals("0"));
            }

        } finally {
            if (app != null) {
                app.stop();
            }
        }
    }


    

}
