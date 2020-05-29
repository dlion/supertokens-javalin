package io.supertokens.javalin.core.querier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.supertokens.javalin.Constants;
import io.supertokens.javalin.core.DeviceInfo;
import io.supertokens.javalin.core.exception.GeneralException;
import io.supertokens.javalin.core.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.*;

public class Querier {

    private static Querier instance;

    private List<STInstance> hosts;

    private String apiVersion = null;

    private int lastTriedIndex = 0;

    private Set<String> hostsAliveForTesting = new HashSet<>();

    @TestOnly
    public static void reset() {
        instance = null;
    }

    @TestOnly
    public Set<String> getHostsAliveForTesting() {
        return hostsAliveForTesting;
    }

    private Querier(String config) throws GeneralException {
        try {
            this.hosts = new ArrayList<>();
            String[] splitted = config.split(";");
            for (String instance : splitted) {
                if (instance.equals("")) {
                    continue;
                }
                String host = instance.split(":")[0];
                int port = Integer.parseInt(instance.split(":")[1]);
                this.hosts.add(new STInstance(host, port));
            }
        } catch (Exception e) {
            throw new GeneralException("Please provide the instance addresses in the correct format: Example: <host1>:<port1>;<host2>:<port2>");
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
                    url -> HttpRequest.sendGETRequest("apiversion", url, null, null),
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

    public <T> T sendPostRequest(String requestID, String path, JsonObject body) throws GeneralException {
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
        return sendRequestHelper(path, url -> HttpRequest.sendJsonPOSTRequest(requestID, url, body, getAPIVersion()), this.hosts.size());
    }

    public <T> T sendDeleteRequest(String requestID, String path, JsonObject body) throws GeneralException {
        return sendRequestHelper(path, url -> HttpRequest.sendJsonDELETERequest(requestID, url, body, getAPIVersion()), this.hosts.size());
    }

    public <T> T sendGetRequest(String requestID, String path, Map<String, String> params) throws GeneralException {
        return sendRequestHelper(path, url -> HttpRequest.sendGETRequest(requestID, url, params, getAPIVersion()), this.hosts.size());
    }

    public <T> T sendPutRequest(String requestID, String path, JsonObject body) throws GeneralException {
        return sendRequestHelper(path, url -> HttpRequest.sendJsonPUTRequest(requestID, url, body, getAPIVersion()), this.hosts.size());
    }

    private <T> T sendRequestHelper(String path, ActualRequest request, int numberOfTries) throws GeneralException {
        if (numberOfTries == 0) {
            throw new GeneralException("No SuperTokens core available to query");
        }
        STInstance currentHost = this.hosts.get(this.lastTriedIndex);
        this.lastTriedIndex = (this.lastTriedIndex + 1) % this.hosts.size();
        try {
            T response = (T) request.handle("http://" + currentHost.host + ":" + currentHost.port + path);
            if (Constants.IS_TESTING) {
                this.hostsAliveForTesting.add(currentHost.host + ":" + currentHost.port);
            }
            return response;
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                return sendRequestHelper(path, request, numberOfTries - 1);
            }
            throw new GeneralException(e);
        }
    }

    @FunctionalInterface
    public interface ActualRequest<T> {
        T handle(@NotNull String url) throws Exception;
    }
}
