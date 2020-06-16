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

import com.google.gson.JsonObject;
import io.supertokens.javalin.Constants;
import io.supertokens.javalin.ProcessState;
import io.supertokens.javalin.SuperTokens;
import io.supertokens.javalin.core.exception.GeneralException;
import io.supertokens.javalin.core.querier.HttpRequestMocking;
import io.supertokens.javalin.core.querier.Querier;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

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
        HttpRequestMocking.reset();
        Constants.IS_TESTING = true;
    }

    @Test
    public void querierCalledWithoutInit() {
        Querier.getInstance();
    }

    @Test
    public void coreNotAvailable() throws Exception {
        SuperTokens.config()
                .withHosts("http://localhost:8080;http://localhost:8081");
        try {
            Querier q = Querier.getInstance();
            q.sendGetRequest("", "/", new HashMap<>());
            throw new Exception("should fail!");
        } catch(GeneralException e) {
            if (!e.getMessage().equals("No SuperTokens core available to query")) {
                throw e;
            }
        }
    }

    @Test
    public void threeCoresAndRoundRobin() throws Exception {
        Utils.startST();
        Utils.startST("localhost", 8081);
        Utils.startST("localhost", 8082);
        SuperTokens.config()
                .withHosts("http://localhost:8080;http://localhost:8081;http://localhost:8082");
        Querier q = Querier.getInstance();
        assert(q.sendGetRequest("", "/hello", new HashMap<>()).equals("Hello"));
        assert(q.sendDeleteRequest("", "/hello", new JsonObject()).equals("Hello"));
        Set<String> hostsAlive = q.getHostsAliveForTesting();
        assert(hostsAlive.size() == 3);
        assert(q.sendGetRequest("", "/hello", new HashMap<>()).equals("Hello")); // this will be the 4th API call
        hostsAlive = q.getHostsAliveForTesting();
        assert(hostsAlive.size() == 3);
        assert(hostsAlive.contains("http://localhost:8080"));
        assert(hostsAlive.contains("http://localhost:8081"));
        assert(hostsAlive.contains("http://localhost:8082"));
    }

    @Test
    public void threeCoresOneDeadRoundRobin() throws Exception {
        Utils.startST();
        Utils.startST("localhost", 8082);
        SuperTokens.config()
                .withHosts("http://localhost:8080;http://localhost:8081;http://localhost:8082");
        Querier q = Querier.getInstance();
        assert(q.sendGetRequest("", "/hello", new HashMap<>()).equals("Hello"));
        assert(q.sendDeleteRequest("", "/hello", new JsonObject()).equals("Hello"));
        Set<String> hostsAlive = q.getHostsAliveForTesting();
        assert(hostsAlive.size() == 2);
        assert(q.sendGetRequest("", "/hello", new HashMap<>()).equals("Hello")); // this will be the 4th API call
        hostsAlive = q.getHostsAliveForTesting();
        assert(hostsAlive.size() == 2);
        assert(hostsAlive.contains("http://localhost:8080"));
        assert(!hostsAlive.contains("http://localhost:8081"));
        assert(hostsAlive.contains("http://localhost:8082"));
    }
}
