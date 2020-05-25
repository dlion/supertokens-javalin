package io.supertokens.javalin.core.Querier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.supertokens.javalin.Constants;
import io.supertokens.javalin.core.DeviceInfo;
import io.supertokens.javalin.core.Exception.GeneralException;
import io.supertokens.javalin.core.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Querier {

    private static Querier instance;

    private List<STInstance> hosts;

    private String apiVersion = null;

    private int lastTriedIndex = 0;

    private Querier(String config) throws GeneralException {
        try {
            this.hosts = new ArrayList<>();
            String[] splitted = config.split(";");
            for (String instance : splitted) {
                String host = instance.split(":")[0];
                int port = Integer.parseInt(instance.split(":")[1]);
                this.hosts.add(new STInstance(host, port));
            }
        } catch (Exception e) {
            throw new GeneralException(e);
        }
    }

    private Querier() {
        this.hosts = new ArrayList<>();
        this.hosts.add(new STInstance("localhost", 3567));
    }

    public static Querier getInstance() {
        if (instance == null) {
            synchronized (Querier.class) {
                if (instance == null) {
                    instance = new Querier();
                }
            }
        }
        return instance;
    }

    public synchronized static void initInstance(String config) throws GeneralException {
        if (instance == null) {
            instance = new Querier(config);
        }
    }

    private String getAPIVersion() throws GeneralException {
        if (this.apiVersion != null) {
            return apiVersion;
        }
        synchronized (Querier.class) {
            if (this.apiVersion != null) {
                return apiVersion;
            }
            JsonObject response = sendRequestHelper("/apiversion",
                    url -> HttpRequest.sendGETRequest(url, null, null),
                    this.hosts.size());
            assert response != null;
            JsonArray cdiSupportedByServerJson = response.getAsJsonArray("versions");
            String[] cdiSupportedByServer = new String[cdiSupportedByServerJson.size()];
            for (int i = 0; i < cdiSupportedByServerJson.size(); i++) {
                cdiSupportedByServer[i] = cdiSupportedByServerJson.get(i).getAsString();
            }
            String supportedVersion = Utils
                    .getLargestVersionFromIntersection(cdiSupportedByServer, Constants.CDI_SUPPORTED);
            if (supportedVersion == null) {
                throw new GeneralException(
                        "The running SuperTokens core version is not compatible with this NodeJS SDK. Please visit https://supertokens.io/docs/community/compatibility to find the right versions");
            }
            this.apiVersion = supportedVersion;
            return supportedVersion;
        }
    }

    public JsonObject sendPostRequest(String path, JsonObject body) throws GeneralException {
        if (path.equals("/session") || path.equals("/session/verify") ||
                path.equals("/session/refresh") || path.equals("/handshake")) {
            DeviceInfo.Device[] devices = DeviceInfo.getInstance().getFrontendSDKs();

            JsonArray frontendSDK = new JsonArray();
            for(DeviceInfo.Device d : devices) {
                JsonObject device = new JsonObject();
                device.addProperty("name", d.name);
                device.addProperty("version", d.version);
                frontendSDK.add(device);
            }

            JsonObject driver = new JsonObject();
            driver.addProperty("name", "javalin");
            driver.addProperty("version", Constants.VERSION);

            body.add("frontendSDK", frontendSDK);
            body.add("driver", driver);
        }
        return sendRequestHelper(path, url -> HttpRequest.sendJsonPOSTRequest(url, body, getAPIVersion()), this.hosts.size());
    }

    public JsonObject sendDeleteRequest(String path, JsonObject body) throws GeneralException {
        return sendRequestHelper(path, url -> HttpRequest.sendJsonDELETERequest(url, body, getAPIVersion()), this.hosts.size());
    }

    public JsonObject sendGetRequest(String path, Map<String, String> params) throws GeneralException {
        return sendRequestHelper(path, url -> HttpRequest.sendGETRequest(url, params, getAPIVersion()), this.hosts.size());
    }

    public JsonObject sendPutRequest(String path, JsonObject body) throws GeneralException {
        return sendRequestHelper(path, url -> HttpRequest.sendJsonPUTRequest(url, body, getAPIVersion()), this.hosts.size());
    }

    private JsonObject sendRequestHelper(String path, ActualRequest request, int numberOfTries) throws GeneralException {
        if (numberOfTries == 0) {
            throw new GeneralException("No SuperTokens core available to query");
        }
        STInstance currentHost = this.hosts.get(this.lastTriedIndex);
        this.lastTriedIndex = (this.lastTriedIndex + 1) % this.hosts.size();
        try {
            return request.handle("http://" + currentHost.host + ":" + currentHost.port + path);
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                return sendRequestHelper(path, request, numberOfTries - 1);
            }
            throw new GeneralException(e);
        }
    }

    @FunctionalInterface
    public interface ActualRequest {
        JsonObject handle(@NotNull String url) throws Exception;
    }
}
