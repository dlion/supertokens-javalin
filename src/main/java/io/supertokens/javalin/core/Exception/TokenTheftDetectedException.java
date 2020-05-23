package io.supertokens.javalin.core.Exception;

public class TokenTheftDetectedException extends SuperTokensException {

    private final String sessionHandle;

    private final String userId;

    public TokenTheftDetectedException(String sessionHandle, String userId) {
        super("Token theft detected");
        this.sessionHandle = sessionHandle;
        this.userId = userId;
    }

    public String getSessionHandle() {
        return this.sessionHandle;
    }

    public String getUserId() {
        return this.userId;
    }

}
