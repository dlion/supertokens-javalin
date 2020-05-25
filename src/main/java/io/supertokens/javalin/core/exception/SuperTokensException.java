package io.supertokens.javalin.core.exception;

public class SuperTokensException extends Exception {

    public SuperTokensException(String msg) {
        super(msg);
    }

    public SuperTokensException(Exception e) {
        super(e);
    }

}
