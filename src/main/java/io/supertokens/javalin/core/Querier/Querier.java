package io.supertokens.javalin.core.Querier;

import com.google.gson.JsonObject;
import io.supertokens.javalin.core.Exception.GeneralException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Querier {

    private static Querier instance;

    private List<STInstance> stInstances;

    private Querier(String config) throws GeneralException {
        try {
            this.stInstances = new ArrayList<>();
            String[] splitted = config.split(";");
            for (String instance : splitted) {
                String host = instance.split(":")[0];
                int port = Integer.parseInt(instance.split(":")[1]);
                this.stInstances.add(new STInstance(host, port));
            }
        } catch (Exception e) {
            throw new GeneralException(e);
        }
    }

    private Querier() {
        this.stInstances = new ArrayList<>();
        this.stInstances.add(new STInstance("localhost", 3567));
    }

    public static Querier getInstance() {
        if (instance == null) {
            instance = new Querier();
        }
        return instance;
    }

    public static void initInstance(String config) throws GeneralException {
        if (instance == null) {
            instance = new Querier(config);
        }
    }

    private String getAPIVersion() {
        // TODO:
        return null;
    }

    public JsonObject sendPostRequest(String path, JsonObject body) throws GeneralException {
        // TODO:
        return null;
    }

    public JsonObject sendDeleteRequest(String path, JsonObject body) throws GeneralException {
        // TODO:
        return null;
    }

    public JsonObject sendGetRequest(String path, Map<String, String> params) throws GeneralException {
        // TODO:
        return null;
    }

    public JsonObject sendPutRequest(String path, JsonObject body) throws GeneralException {
        // TODO:
        return null;
    }

    private JsonObject sendRequestHelper(String path, ActualRequest request, int numberOfTries) throws GeneralException {
        // TODO:
        return null;
    }

    @FunctionalInterface
    public interface ActualRequest {
        JsonObject handle(@NotNull String url) throws Exception;
    }
}
