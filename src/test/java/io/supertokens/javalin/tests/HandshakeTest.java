package io.supertokens.javalin.tests;

import io.supertokens.javalin.Constants;
import io.supertokens.javalin.ProcessState;
import io.supertokens.javalin.SuperTokens;
import io.supertokens.javalin.core.HandshakeInfo;
import io.supertokens.javalin.core.querier.HttpRequestMocking;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class HandshakeTest {

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
        SuperTokens.config("localhost:8080;");

        HandshakeInfo info = HandshakeInfo.getInstance();
        assert(info.accessTokenPath.equals("/"));
        assert(info.cookieDomain.equals("supertokens.io"));
        assert(!info.cookieSecure);
        assert(info.refreshTokenPath.equals("/refresh"));
        assert(info.enableAntiCsrf);
        assert(!info.accessTokenBlacklistingEnabled);
        info.updateJwtSigningPublicKeyInfo("hello", 100);
        HandshakeInfo info2 = HandshakeInfo.getInstance();
        assert(info2.jwtSigningPublicKey.equals("hello"));
        assert(info2.jwtSigningPublicKeyExpiryTime == 100);

    }
}
