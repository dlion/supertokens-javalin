package io.supertokens.javalin.core.Exception;

public class GeneralException extends SuperTokensException {

    public GeneralException(Exception e) {
        super("General exception thrown from SuperTokens", e);
    }
}
