package io.supertokens.javalin.tests;

import io.supertokens.javalin.Constants;
import io.supertokens.javalin.ProcessState;
import io.supertokens.javalin.core.querier.HttpRequestMocking;
import org.junit.AfterClass;
import org.junit.Before;

import java.io.IOException;

public class DeviceDriverInfoTest {

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


}
