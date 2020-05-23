package io.supertokens.javalin.core.Exception;

public class SuperTokensException extends Exception {

    public SuperTokensException(String msg) {
        super(msg);
    }

    public SuperTokensException(String msg, Exception e) {
        super(msg, e);
    }

}
