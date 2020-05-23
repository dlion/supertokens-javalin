package io.supertokens.javalin.core.Exception;

public class GeneralException extends SuperTokensException {
    public GeneralException(Exception e) {
        super(e);
    }

    public GeneralException(String msg) {
        super(msg);
    }
}
