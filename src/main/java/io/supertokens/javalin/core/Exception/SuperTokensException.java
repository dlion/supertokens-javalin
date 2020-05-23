package io.supertokens.javalin.core.Exception;

public class SuperTokensException extends Exception {

    public SuperTokensException(Exception e) {
        super(e);
    }

    public SuperTokensException(String msg) {
        super(msg);
    }

}
