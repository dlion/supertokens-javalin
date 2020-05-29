package io.supertokens.javalin.core.querier;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.supertokens.javalin.Constants;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpRequest {
    private static final int STATUS_CODE_ERROR_THRESHOLD = 400;

    private static URL getURL(String requestID, String url) throws MalformedURLException {
        URL obj = new URL(url);
        if (Constants.IS_TESTING) {
            URL mock = HttpRequestMocking.getInstance().getMockURL(requestID, url);
            if (mock != null) {
                obj = mock;
            }
        }
        return obj;
    }

    private static boolean isJsonValid(String jsonInString) {
        JsonElement el = null;
        try {
            el = new JsonParser().parse(jsonInString);
            el.getAsJsonObject();
            return true;
        } catch (Exception ex) {
            try {
                assert el != null;
                el.getAsJsonArray();
                return true;
            } catch (Throwable e) {
                return false;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T sendGETRequest(String requestID, String url, Map<String, String> params, String version)
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
        URL obj = getURL(requestID, url);
        InputStream inputStream = null;
        HttpURLConnection con = null;

        try {
            con = (HttpURLConnection) obj.openConnection();
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            if (version != null) {
                con.setRequestProperty("cdi-version", version);
            }

            int responseCode = con.getResponseCode();

            if (responseCode < STATUS_CODE_ERROR_THRESHOLD) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            if (responseCode < STATUS_CODE_ERROR_THRESHOLD) {
                if (!isJsonValid(response.toString())) {
                    return (T) response.toString();
                }
                return (T) (new JsonParser().parse(response.toString()));
            }
            throw new HttpResponseException(responseCode, response.toString());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

            if (con != null) {
                con.disconnect();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T sendJsonRequest(String requestID, String url, JsonElement requestBody, String version, String method)
            throws IOException, HttpResponseException {
        URL obj = getURL(requestID, url);;
        InputStream inputStream = null;
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod(method);
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            if (version != null) {
                con.setRequestProperty("cdi-version", version);
            }

            if (requestBody != null) {
                con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            int responseCode = con.getResponseCode();

            if (responseCode < STATUS_CODE_ERROR_THRESHOLD) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            if (responseCode < STATUS_CODE_ERROR_THRESHOLD) {
                if (!isJsonValid(response.toString())) {
                    return (T) response.toString();
                }
                return (T) (new JsonParser().parse(response.toString()));
            }
            throw new HttpResponseException(responseCode, response.toString());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

            if (con != null) {
                con.disconnect();
            }
        }
    }

    public static <T> T sendJsonPOSTRequest(String requestID, String url, JsonElement requestBody, String version)
            throws IOException, HttpResponseException {
        return sendJsonRequest(requestID, url, requestBody, version, "POST");
    }

    public static <T> T sendJsonPUTRequest(String requestID, String url, JsonElement requestBody, String version)
            throws IOException, HttpResponseException {
        return sendJsonRequest(requestID, url, requestBody, version, "PUT");
    }

    public static <T> T sendJsonDELETERequest(String requestID, String url, JsonElement requestBody, String version)
            throws IOException, HttpResponseException {
        return sendJsonRequest(requestID, url, requestBody, version,
                "DELETE");
    }
}
