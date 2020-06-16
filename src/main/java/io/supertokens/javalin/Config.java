/*
 * Copyright (c) 2020, VRAI Labs and/or its affiliates. All rights reserved.
 *
 * This software is licensed under the Apache License, Version 2.0 (the "License") as published by the Apache Software Foundation.
 *
 * You may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package io.supertokens.javalin;

import io.supertokens.javalin.core.SessionFunctions;

public class Config {

    private static Config instance = null;

     String hosts = null;

     String accessTokenPath = null;

     String refreshApiPath = null;

     String cookieDomain = null;

     Boolean cookieSecure = null;

     String cookieSameSite = null;

    private Config(){}

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public Config withHosts(String hosts) {
        this.hosts = hosts;
        SessionFunctions.config(hosts);
        return this;
    }

    public Config withRefreshApiPath(String path) {
        this.refreshApiPath = path;
        return this;
    }

    public Config withAccessTokenPath(String path) {
        this.accessTokenPath = path;
        return this;
    }

    public Config withCookieDomain(String domain){
        this.cookieDomain = domain;
        return this;
    }

    public Config withCookieSecure(boolean cookieSecure) {
        this.cookieSecure = cookieSecure;
        return this;
    }

    public Config withCookieSameSite(String cookieSameSite ) {
        this.cookieSameSite = cookieSameSite;
        return this;
    }

}
