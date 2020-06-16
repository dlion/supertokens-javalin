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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;
import io.supertokens.javalin.Constants;
import io.supertokens.javalin.ProcessState;
import io.supertokens.javalin.SuperTokens;
import io.supertokens.javalin.core.HandshakeInfo;
import io.supertokens.javalin.core.SessionFunctions;
import io.supertokens.javalin.core.querier.HttpRequestMocking;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import io.supertokens.javalin.tests.httprequest.HttpRequest;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DeviceDriverInfoTest extends Mockito {

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

        final HttpsURLConnection mockCon = mock(HttpsURLConnection.class);
        boolean success = false;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        InputStream inputStrm = new ByteArrayInputStream(("").getBytes(StandardCharsets.UTF_8));
        when(mockCon.getInputStream()).thenReturn(inputStrm);
        when(mockCon.getResponseCode()).thenReturn(200);
        when(mockCon.getOutputStream()).thenReturn(new OutputStream() {
            @Override
            public void write(int b) {
                output.write(b);
            }
        });

        HttpRequestMocking.getInstance().setMockURL("newsession", new HttpRequestMocking.URLGetter(){

            @Override
            public URL getUrl(String url) throws MalformedURLException {
                URLStreamHandler stubURLStreamHandler = new URLStreamHandler() {
                    @Override
                    protected URLConnection openConnection(URL u) {
                        return mockCon;
                    }
                };
                return new URL(null, url, stubURLStreamHandler);
            }
        });

        try {
            SessionFunctions.createNewSession("", new JsonObject(), new JsonObject());
        } catch (Exception ignored) {}

        JsonObject outputJson = new JsonParser().parse(output.toString()).getAsJsonObject();

        JsonArray frontendSDK = outputJson.getAsJsonArray("frontendSDK");
        JsonObject driver = outputJson.getAsJsonObject("driver");
        String name = driver.get("name").getAsString();
        String version = driver.get("version").getAsString();
        assert(frontendSDK.size() == 0 && name.equals("javalin") && version.equals(Constants.VERSION));
    }

    @Test
    public void frontendSDKTest() throws Exception {
        Utils.startST();
        SuperTokens.config()
                .withHosts("http://localhost:8080");
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
                headers.put("supertokens-sdk-name", "ios");
                headers.put("supertokens-sdk-version", "0.0.0");
                HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/verify", new JsonObject(), headers);
            }

            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response.get("accessToken") + ";sIdRefreshToken=" +
                        response.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", response.get("antiCsrf"));
                HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/verify", new JsonObject(), headers);
            }

            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", "sAccessToken=" + response.get("accessToken") + ";sIdRefreshToken=" +
                        response.get("idRefreshTokenFromCookie"));
                headers.put("anti-csrf", response.get("antiCsrf"));
                headers.put("supertokens-sdk-name", "android");
                headers.put("supertokens-sdk-version", "0.0.1");
                HttpRequest.sendJsonPOSTRequest("http://localhost:8081/session/verify", new JsonObject(), headers);
            }

            ByteArrayOutputStream createSessionOutput;
            {
                final HttpsURLConnection mockCon = mock(HttpsURLConnection.class);
                createSessionOutput = new ByteArrayOutputStream();

                InputStream inputStrm = new ByteArrayInputStream(("").getBytes(StandardCharsets.UTF_8));
                when(mockCon.getInputStream()).thenReturn(inputStrm);
                when(mockCon.getResponseCode()).thenReturn(200);
                when(mockCon.getOutputStream()).thenReturn(new OutputStream() {
                    @Override
                    public void write(int b) {
                        createSessionOutput.write(b);
                    }
                });
                HttpRequestMocking.getInstance().setMockURL("newsession", new HttpRequestMocking.URLGetter(){
                    @Override
                    public URL getUrl(String url) throws MalformedURLException {
                        URLStreamHandler stubURLStreamHandler = new URLStreamHandler() {
                            @Override
                            protected URLConnection openConnection(URL u) {
                                return mockCon;
                            }
                        };
                        return new URL(null, url, stubURLStreamHandler);
                    }
                });
            }

            ByteArrayOutputStream verifySessionOutput;
            {
                final HttpsURLConnection mockCon = mock(HttpsURLConnection.class);
                verifySessionOutput = new ByteArrayOutputStream();

                InputStream inputStrm = new ByteArrayInputStream(("").getBytes(StandardCharsets.UTF_8));
                when(mockCon.getInputStream()).thenReturn(inputStrm);
                when(mockCon.getResponseCode()).thenReturn(200);
                when(mockCon.getOutputStream()).thenReturn(new OutputStream() {
                    @Override
                    public void write(int b) {
                        verifySessionOutput.write(b);
                    }
                });
                HttpRequestMocking.getInstance().setMockURL("getsession", new HttpRequestMocking.URLGetter(){
                    @Override
                    public URL getUrl(String url) throws MalformedURLException {
                        URLStreamHandler stubURLStreamHandler = new URLStreamHandler() {
                            @Override
                            protected URLConnection openConnection(URL u) {
                                return mockCon;
                            }
                        };
                        return new URL(null, url, stubURLStreamHandler);
                    }
                });
            }

            ByteArrayOutputStream refreshOutput;
            {
                final HttpsURLConnection mockCon = mock(HttpsURLConnection.class);
                refreshOutput = new ByteArrayOutputStream();

                InputStream inputStrm = new ByteArrayInputStream(("").getBytes(StandardCharsets.UTF_8));
                when(mockCon.getInputStream()).thenReturn(inputStrm);
                when(mockCon.getResponseCode()).thenReturn(200);
                when(mockCon.getOutputStream()).thenReturn(new OutputStream() {
                    @Override
                    public void write(int b) {
                        refreshOutput.write(b);
                    }
                });
                HttpRequestMocking.getInstance().setMockURL("refresh", new HttpRequestMocking.URLGetter(){
                    @Override
                    public URL getUrl(String url) throws MalformedURLException {
                        URLStreamHandler stubURLStreamHandler = new URLStreamHandler() {
                            @Override
                            protected URLConnection openConnection(URL u) {
                                return mockCon;
                            }
                        };
                        return new URL(null, url, stubURLStreamHandler);
                    }
                });
            }

            ByteArrayOutputStream handshakeOutput;
            {
                final HttpsURLConnection mockCon = mock(HttpsURLConnection.class);
                handshakeOutput = new ByteArrayOutputStream();

                InputStream inputStrm = new ByteArrayInputStream(("").getBytes(StandardCharsets.UTF_8));
                when(mockCon.getInputStream()).thenReturn(inputStrm);
                when(mockCon.getResponseCode()).thenReturn(200);
                when(mockCon.getOutputStream()).thenReturn(new OutputStream() {
                    @Override
                    public void write(int b) {
                        handshakeOutput.write(b);
                    }
                });
                HttpRequestMocking.getInstance().setMockURL("handshake", new HttpRequestMocking.URLGetter(){
                    @Override
                    public URL getUrl(String url) throws MalformedURLException {
                        URLStreamHandler stubURLStreamHandler = new URLStreamHandler() {
                            @Override
                            protected URLConnection openConnection(URL u) {
                                return mockCon;
                            }
                        };
                        return new URL(null, url, stubURLStreamHandler);
                    }
                });
            }

            // create new session check.
            {
                try {
                    SessionFunctions.createNewSession("", new JsonObject(), new JsonObject());
                } catch (Exception ignored) {}
                JsonObject outputJson = new JsonParser().parse(createSessionOutput.toString()).getAsJsonObject();

                JsonArray frontendSDK = outputJson.getAsJsonArray("frontendSDK");
                assert (frontendSDK.size() == 2);
                {
                    JsonObject sdk = frontendSDK.get(0).getAsJsonObject();
                    String name = sdk.get("name").getAsString();
                    String version = sdk.get("version").getAsString();
                    assert (name.equals("ios") && version.equals("0.0.0"));
                }
                {
                    JsonObject sdk = frontendSDK.get(1).getAsJsonObject();
                    String name = sdk.get("name").getAsString();
                    String version = sdk.get("version").getAsString();
                    assert (name.equals("android") && version.equals("0.0.1"));
                }

                JsonObject driver = outputJson.getAsJsonObject("driver");
                String name = driver.get("name").getAsString();
                String version = driver.get("version").getAsString();
                assert (name.equals("javalin") && version.equals(Constants.VERSION));
            }

            // get session check.
            {
                try {
                    SessionFunctions.getSession("", "", false);
                } catch (Exception ignored) {}
                JsonObject outputJson = new JsonParser().parse(verifySessionOutput.toString()).getAsJsonObject();

                JsonArray frontendSDK = outputJson.getAsJsonArray("frontendSDK");
                assert (frontendSDK.size() == 2);
                {
                    JsonObject sdk = frontendSDK.get(0).getAsJsonObject();
                    String name = sdk.get("name").getAsString();
                    String version = sdk.get("version").getAsString();
                    assert (name.equals("ios") && version.equals("0.0.0"));
                }
                {
                    JsonObject sdk = frontendSDK.get(1).getAsJsonObject();
                    String name = sdk.get("name").getAsString();
                    String version = sdk.get("version").getAsString();
                    assert (name.equals("android") && version.equals("0.0.1"));
                }

                JsonObject driver = outputJson.getAsJsonObject("driver");
                String name = driver.get("name").getAsString();
                String version = driver.get("version").getAsString();
                assert (name.equals("javalin") && version.equals(Constants.VERSION));
            }

            // refresh session check.
            {
                try {
                    SessionFunctions.refreshSession("");
                } catch (Exception ignored) {}
                JsonObject outputJson = new JsonParser().parse(refreshOutput.toString()).getAsJsonObject();

                JsonArray frontendSDK = outputJson.getAsJsonArray("frontendSDK");
                assert (frontendSDK.size() == 2);
                {
                    JsonObject sdk = frontendSDK.get(0).getAsJsonObject();
                    String name = sdk.get("name").getAsString();
                    String version = sdk.get("version").getAsString();
                    assert (name.equals("ios") && version.equals("0.0.0"));
                }
                {
                    JsonObject sdk = frontendSDK.get(1).getAsJsonObject();
                    String name = sdk.get("name").getAsString();
                    String version = sdk.get("version").getAsString();
                    assert (name.equals("android") && version.equals("0.0.1"));
                }

                JsonObject driver = outputJson.getAsJsonObject("driver");
                String name = driver.get("name").getAsString();
                String version = driver.get("version").getAsString();
                assert (name.equals("javalin") && version.equals(Constants.VERSION));
            }

            // handshake check.
            {
                HandshakeInfo.reset();
                try {
                    HandshakeInfo.getInstance();
                } catch (Exception ignored) {}
                JsonObject outputJson = new JsonParser().parse(handshakeOutput.toString()).getAsJsonObject();

                JsonArray frontendSDK = outputJson.getAsJsonArray("frontendSDK");
                assert (frontendSDK.size() == 2);
                {
                    JsonObject sdk = frontendSDK.get(0).getAsJsonObject();
                    String name = sdk.get("name").getAsString();
                    String version = sdk.get("version").getAsString();
                    assert (name.equals("ios") && version.equals("0.0.0"));
                }
                {
                    JsonObject sdk = frontendSDK.get(1).getAsJsonObject();
                    String name = sdk.get("name").getAsString();
                    String version = sdk.get("version").getAsString();
                    assert (name.equals("android") && version.equals("0.0.1"));
                }

                JsonObject driver = outputJson.getAsJsonObject("driver");
                String name = driver.get("name").getAsString();
                String version = driver.get("version").getAsString();
                assert (name.equals("javalin") && version.equals(Constants.VERSION));
            }
        } finally {
            if (app != null) {
                app.stop();
            }
        }
    }


}
