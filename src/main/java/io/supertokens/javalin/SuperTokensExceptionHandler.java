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

package io.supertokens.javalin;

import io.javalin.http.Context;
import io.javalin.http.ExceptionHandler;
import io.supertokens.javalin.core.exception.*;
import io.supertokens.javalin.core.HandshakeInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SuperTokensExceptionHandler implements ExceptionHandler<SuperTokensException> {

    private ExceptionHandler<UnauthorisedException> unauthorisedHandler;
    private ExceptionHandler<TryRefreshTokenException> tryRefreshTokenHandler;
    private ExceptionHandler<TokenTheftDetectedException> tokenTheftDetectedHandler;
    private ExceptionHandler<GeneralException> generalExceptionHandler;

    public SuperTokensExceptionHandler onUnauthorisedError(ExceptionHandler<UnauthorisedException> exceptionHandler) {
        this.unauthorisedHandler = exceptionHandler;
        return this;
    }

    public SuperTokensExceptionHandler onTryRefreshTokenError(ExceptionHandler<TryRefreshTokenException> exceptionHandler) {
        this.tryRefreshTokenHandler = exceptionHandler;
        return this;
    }

    public SuperTokensExceptionHandler onTokenTheftDetectedError(ExceptionHandler<TokenTheftDetectedException> exceptionHandler) {
        this.tokenTheftDetectedHandler = exceptionHandler;
        return this;
    }

    public SuperTokensExceptionHandler onGeneralError(ExceptionHandler<GeneralException> exceptionHandler) {
        this.generalExceptionHandler = exceptionHandler;
        return this;
    }

    @Override
    public void handle(@NotNull SuperTokensException exception, @NotNull Context ctx) {
        if (exception instanceof UnauthorisedException) {
            if (this.unauthorisedHandler == null) {
                defaultUnauthorisedExceptionHandler((UnauthorisedException) exception, ctx);
            } else {
                this.unauthorisedHandler.handle((UnauthorisedException) exception, ctx);
            }
        } else if (exception instanceof TryRefreshTokenException) {
            if (this.tryRefreshTokenHandler == null) {
                defaultTryRefreshTokenExceptionHandler((TryRefreshTokenException) exception, ctx);
            } else {
                this.tryRefreshTokenHandler.handle((TryRefreshTokenException) exception, ctx);
            }
        } else if (exception instanceof TokenTheftDetectedException) {
            if (this.tokenTheftDetectedHandler == null) {
                defaultTokenTheftDetectedExceptionHandler((TokenTheftDetectedException) exception, ctx);
            } else {
                this.tokenTheftDetectedHandler.handle((TokenTheftDetectedException) exception, ctx);
            }
        } else {
            if (this.generalExceptionHandler == null) {
                defaultGeneralExceptionHandler((GeneralException) exception, ctx);
            } else {
                this.generalExceptionHandler.handle((GeneralException) exception, ctx);
            }
        }
    }

    private void defaultUnauthorisedExceptionHandler(@NotNull UnauthorisedException exception, @NotNull Context ctx) {
        try {
            HandshakeInfo handshakeInfo = HandshakeInfo.getInstance();
            ctx.status(handshakeInfo.sessionExpiredStatusCode).result("unauthorised");
        } catch (GeneralException e) {
            this.handle(e, ctx);
        }
    }

    private void defaultTryRefreshTokenExceptionHandler(@NotNull TryRefreshTokenException exception, @NotNull Context ctx) {
        try {
            HandshakeInfo handshakeInfo = HandshakeInfo.getInstance();
            ctx.status(handshakeInfo.sessionExpiredStatusCode).result("try refresh token");
        } catch (GeneralException e) {
            this.handle(e, ctx);
        }
    }

    private void defaultTokenTheftDetectedExceptionHandler(@NotNull TokenTheftDetectedException exception, @NotNull Context ctx) {
        try {
            HandshakeInfo handshakeInfo = HandshakeInfo.getInstance();
            boolean ignored = SuperTokens.revokeSession(exception.getSessionHandle());
            ctx.status(handshakeInfo.sessionExpiredStatusCode).result("token theft detected");
        } catch (GeneralException e) {
            this.handle(e, ctx);
        }
    }

    private void defaultGeneralExceptionHandler(@NotNull GeneralException exception, @NotNull Context ctx) {
        Throwable cause = exception.getCause();
        String message;
        message = Objects.requireNonNullElse(cause, exception).getMessage();
        ctx.status(500).result(message);
    }
}
