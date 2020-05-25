package io.supertokens.javalin.core.accessToken;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.supertokens.javalin.core.Utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public class JWT {
    private static String HEADERv2 = null;

    private static void initHeader() {
        if (HEADERv2 == null) {
            JsonObject header = new JsonObject();
            header.addProperty("alg", "RS256");
            header.addProperty("typ", "JWT");
            header.addProperty("version", "2");
            JWT.HEADERv2 = Utils.convertToBase64(header.toString());
        }
    }

    public static JsonObject verifyJWTAndGetPayload(String jwt, String publicSigningKey)
            throws InvalidKeyException, NoSuchAlgorithmException, JWTException {
        initHeader();
        String[] splittedInput = jwt.split("\\.");
        if (splittedInput.length != 3) {
            throw new JWTException("Invalid JWT");
        }
        // checking header
        if (!splittedInput[0].equals(JWT.HEADERv2)) {
            throw new JWTException("JWT header mismatch");
        }
        // verifying signature
        String payload = splittedInput[1];
        try {
            if (!Utils.verifyWithPublicKey(splittedInput[0] + "." + payload, splittedInput[2], publicSigningKey)) {
                throw new JWTException("JWT verification failed");
            }
        } catch (InvalidKeySpecException | SignatureException e) {
            throw new JWTException("JWT verification failed");
        }
        return (JsonObject) new JsonParser().parse(Utils.convertFromBase64(splittedInput[1]));
    }

    public static class JWTException extends Exception {

        private static final long serialVersionUID = 1L;

        JWTException(String err) {
            super(err);
        }
    }
}
