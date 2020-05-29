package io.supertokens.javalin.tests.httprequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.supertokens.javalin.Constants;
import io.supertokens.javalin.core.querier.HttpRequestMocking;
import io.supertokens.javalin.core.querier.HttpResponseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpRequest {
    public static HttpURLConnection sendGETRequest(String url, Map<String, String> params, Map<String, String> headers)
            throws IOException, HttpResponseException {
        StringBuilder paramBuilder = new StringBuilder();

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                paramBuilder.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(),
                        StandardCharsets.UTF_8))
                        .append("&");
            }
        }
        String paramsStr = paramBuilder.toString();
        if (!paramsStr.equals("")) {
            paramsStr = paramsStr.substring(0, paramsStr.length() - 1);
            url = url + "?" + paramsStr;
        }
        URL obj = new URL(url);
        HttpURLConnection con = null;

        try {
            con = (HttpURLConnection) obj.openConnection();
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            if (headers != null) {
                headers.forEach(con::setRequestProperty);
            }

            con.getResponseCode();
            return con;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    private static HttpURLConnection sendJsonRequest(String url, JsonElement requestBody, Map<String, String> headers, String method)
            throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod(method);
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            if (headers != null) {
                headers.forEach(con::setRequestProperty);
            }

            if (requestBody != null) {
                con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }
            con.getResponseCode();
            return con;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    public static HttpURLConnection sendJsonPOSTRequest(String url, JsonElement requestBody, Map<String, String> headers)
            throws IOException, HttpResponseException {
        return sendJsonRequest(url, requestBody, headers, "POST");
    }

    public static HttpURLConnection sendJsonPUTRequest(String url, JsonElement requestBody,  Map<String, String> headers)
            throws IOException, HttpResponseException {
        return sendJsonRequest(url, requestBody, headers, "PUT");
    }

    public static HttpURLConnection sendJsonDELETERequest(String url, JsonElement requestBody, Map<String, String> headers)
            throws IOException, HttpResponseException {
        return sendJsonRequest(url, requestBody, headers,
                "DELETE");
    }
}
