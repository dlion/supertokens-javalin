package io.supertokens.javalin.tests;

import io.supertokens.javalin.Constants;
import io.supertokens.javalin.ProcessState;
import io.supertokens.javalin.SuperTokens;
import io.supertokens.javalin.core.exception.GeneralException;
import io.supertokens.javalin.core.querier.Querier;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class QuerierTest {

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
        Constants.IS_TESTING = true;
    }

    @Test
    public void querierCalledWithoutInit() {
        Querier.getInstance();
    }

    @Test
    public void coreNotAvailable() throws Exception {
        SuperTokens.config("localhost:8080;localhost:8081");
        try {
            Querier q = Querier.getInstance();
            q.sendGetRequest("/", new HashMap<>());
            throw new Exception("should fail!");
        } catch(GeneralException e) {
            if (!e.getMessage().equals("No SuperTokens core available to query")) {
                throw e;
            }
        }
    }

    @Test
    public void threeCoresAndRoundRobin() throws Exception {
        // TODO:
    }
}
