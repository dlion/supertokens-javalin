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
