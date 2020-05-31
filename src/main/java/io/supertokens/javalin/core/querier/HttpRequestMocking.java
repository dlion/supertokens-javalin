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

package io.supertokens.javalin.core.querier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestMocking {

    private Map<String, URLGetter> urlMap = new HashMap<>();

    private static HttpRequestMocking instance;

    private HttpRequestMocking() {
    }

    public static void reset() {
        instance = null;
    }

    public static HttpRequestMocking getInstance() {
        if (instance == null) {
            instance = new HttpRequestMocking();
        }
        return instance;
    }

    public void setMockURL(String key, URLGetter urlGetter) {
        urlMap.put(key, urlGetter);
    }

    URL getMockURL(String key, String url) throws MalformedURLException {
        URLGetter urlGetter = urlMap.get(key);
        if (urlGetter != null) {
            return urlGetter.getUrl(url);
        }
        return null;
    }

    public abstract static class URLGetter {
        public abstract URL getUrl(String url) throws MalformedURLException;
    }
}
