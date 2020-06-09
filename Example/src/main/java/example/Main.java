package example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.json.JavalinJson;
import io.supertokens.javalin.*;
import io.javalin.Javalin;
import io.supertokens.javalin.core.HandshakeInfo;
import io.supertokens.javalin.core.exception.GeneralException;
import io.supertokens.javalin.core.exception.SuperTokensException;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Main {

    static int noOfTimesGetSessionCalledDuringTest = 0;
    static int noOfTimesRefreshCalledDuringTest = 0;

    public static void main(String[] args) throws GeneralException {
        SuperTokens.config("localhost:9000");
        Javalin app = Javalin.create(config -> {
            config.addStaticFiles(System.getProperty("user.dir") + "/public", Location.EXTERNAL);
        }).start("0.0.0.0", 8080);

        app.options("*", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "http://127.0.0.1:8080");
            ctx.header("Access-Control-Allow-Headers", "content-type");
            ctx.header("Access-Control-Allow-Methods", "*");
            SuperTokens.setRelevantHeadersForOptionsAPI(ctx);
            ctx.result("");
        });

        app.post("/login", ctx -> {
            JsonObject body = new JsonParser().parse(ctx.body()).getAsJsonObject();
            String userId = body.get("userId").getAsString();
            Session session = SuperTokens.newSession(ctx, userId).create();
            ctx.header("Access-Control-Allow-Origin", "http://127.0.0.1:8080");
            ctx.header("Access-Control-Allow-Credentials", "true");
            ctx.result(userId);
        });

        app.post("/testUserConfig", ctx -> {
            ctx.result("");
        });

        app.post("/multipleInterceptors", ctx -> {
            String interceptorheader2 = ctx.header("interceptorheader2");
            String interceptorheader1 = ctx.header("interceptorheader1");
            ctx.result(interceptorheader2 != null && interceptorheader1 != null ? "success" : "failure");
        });

        app.before("/", SuperTokens.middleware());
        app.get("/", ctx -> {
            noOfTimesGetSessionCalledDuringTest += 1;
            String userId = SuperTokens.getFromContext(ctx).getUserId();
            ctx.header("Access-Control-Allow-Origin", "http://127.0.0.1:8080");
            ctx.header("Access-Control-Allow-Credentials", "true");
            ctx.result(userId);
        });

        app.before("/update-jwt", SuperTokens.middleware());
        app.get("/update-jwt", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "http://127.0.0.1:8080");
            ctx.header("Access-Control-Allow-Credentials", "true");
            ctx.json(SuperTokens.getFromContext(ctx).getJWTPayload());
        });

        app.post("/update-jwt", ctx -> {
            JsonObject body = new JsonParser().parse(ctx.body()).getAsJsonObject();
            SuperTokens.getFromContext(ctx).updateJWTPayload(new Gson().fromJson(body.toString(), new TypeToken<Map<String, Object>>(){}.getType()));
            ctx.header("Access-Control-Allow-Origin", "http://127.0.0.1:8080");
            ctx.header("Access-Control-Allow-Credentials", "true");
            ctx.json(SuperTokens.getFromContext(ctx).getJWTPayload());
        });

        app.post("/beforeeach", ctx -> {
            noOfTimesRefreshCalledDuringTest = 0;
            noOfTimesGetSessionCalledDuringTest = 0;
            HandshakeInfo.reset();
            ctx.result("");
        });

        Handler testing = new Handler() {
            @Override
            public void handle(@NotNull Context ctx) throws Exception {
                String value = ctx.header("testing");
                if (value != null) {
                    ctx.header("testing", value);
                }
                ctx.result("success");
            }
        };

        app.get("/testing", testing);
        app.post("/testing", testing);
        app.delete("/testing", testing);
        app.put("/testing", testing);


        app.before("/logout", SuperTokens.middleware());
        app.post("/logout", ctx -> {
            Session session =  SuperTokens.getFromContext(ctx);
            session.revokeSession();
            ctx.header("Access-Control-Allow-Origin", "http://127.0.0.1:8080");
            ctx.header("Access-Control-Allow-Credentials", "true");
            ctx.result("success");
        });

        app.before("/revokeAll", SuperTokens.middleware());
        app.post("/revokeAll", ctx -> {
            Session session =  SuperTokens.getFromContext(ctx);
            String userId = session.getUserId();
            SuperTokens.revokeAllSessionsForUser(userId);
            ctx.result("success");
        });

        app.before("/refresh", SuperTokens.middleware());
        app.post("/refresh", ctx -> {
            noOfTimesRefreshCalledDuringTest++;
            ctx.header("Access-Control-Allow-Origin", "http://127.0.0.1:8080");
            ctx.header("Access-Control-Allow-Credentials", "true");
            ctx.result("refresh success");
        });

        app.get("/refreshCalledTime", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "http://127.0.0.1:8080");
            ctx.result("" + noOfTimesRefreshCalledDuringTest);
        });

        app.get("/getSessionCalledTime", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "http://127.0.0.1:8080");
            ctx.result("" + noOfTimesGetSessionCalledDuringTest);
        });

        app.get("/getPackageVersion", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "http://127.0.0.1:8080");
            ctx.result("4.1.4");
        });

        app.get("/ping", ctx -> {
            ctx.result("");
        });

        app.get("/testHeader", ctx -> {
            String testHeader = ctx.header("st-custom-header");
            boolean success = testHeader != null;
            JsonObject result = new JsonObject();
            result.addProperty("success", success);
            ctx.contentType("application/json").result(result.toString());
        });

        app.get("/checkDeviceInfo", ctx -> {
            String sdkName = ctx.header("supertokens-sdk-name");
            String sdkVersion = ctx.header("supertokens-sdk-version");
            ctx.result(sdkName.equals("website") && sdkVersion.equals("4.1.4") ? "true" : "false");
        });

        app.post("/checkAllowCredentials", ctx -> {
            ctx.result(ctx.header("allow-credentials") != null ? "true" : "false");
        });

        app.get("/testError", ctx -> {
            ctx.status(500).result("test error message");
        });

        app.exception(SuperTokensException.class, SuperTokens.exceptionHandler()
                .onTryRefreshTokenError((exception, ctx) -> {
                    ctx.header("Access-Control-Allow-Origin", "http://127.0.0.1:8080");
                    ctx.header("Access-Control-Allow-Credentials", "true");
                    ctx.status(440).result("");
                })
                .onUnauthorisedError((exception, ctx) -> {
                    ctx.header("Access-Control-Allow-Origin", "http://127.0.0.1:8080");
                    ctx.header("Access-Control-Allow-Credentials", "true");
                    ctx.status(440).result("");
                })
                .onGeneralError(((exception, ctx) -> {
                    exception.printStackTrace();
                    ctx.status(500).result("Something went wrong");
                })));

    }
}
