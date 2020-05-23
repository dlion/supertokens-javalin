package io.supertokens.javalin.core.InformationHolders;

public class TokenInfo {
    public final String token;
    public final long expiry;
    public final long createdTime;
    public final String cookiePath;
    public final boolean cookieSecure;
    public final String domain;
    public final String sameSite;

    public TokenInfo(String token, long expiry, long createdTime,
                     String cookiePath, boolean cookieSecure, String domain, String sameSite) {
        this.token = token;
        this.expiry = expiry;
        this.createdTime = createdTime;
        this.cookiePath = cookiePath;
        this.cookieSecure = cookieSecure;
        this.domain = domain;
        this.sameSite = sameSite;
    }
}
