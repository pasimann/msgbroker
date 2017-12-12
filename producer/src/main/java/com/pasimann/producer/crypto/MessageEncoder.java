package com.pasimann.producer.crypto;

import java.util.ResourceBundle;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.AmqpException;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MessageEncoder {
    private static final Logger log = LoggerFactory.getLogger(MessageEncoder.class);

    private final static String PROPERTIES_FILENAME = "PRODUCER";
    private static ResourceBundle propertiesBundle;
    private static String secretKey;

    private final static String SPRING_CRYPTO_METHOD_AES = "AES";

    static {
       propertiesBundle = ResourceBundle.getBundle(PROPERTIES_FILENAME);

        secretKey = propertiesBundle.getString("producer.secretkey");

        if (System.getenv("SPRING_CRYPTO_SECRET_KEY") != null) {
			secretKey = System.getenv("SPRING_CRYPTO_SECRET_KEY");
		}
    }

    public static String encodeMessage(byte[] data) {

        String result = null;

        try {

            // Create key and cipher
            Key aesKey = new SecretKeySpec(secretKey.getBytes(), SPRING_CRYPTO_METHOD_AES);
            Cipher cipher = Cipher.getInstance(SPRING_CRYPTO_METHOD_AES);

            // encrypt the text
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encrypted = cipher.doFinal(data);

            // base64 encode the crypted data
            byte[] base64Data = Base64.getEncoder().encode(encrypted);

            result = new String(base64Data, StandardCharsets.UTF_8);
            return result;

        } catch (Exception e) {
            log.error("Exception when encrypting message.", e);
            throw new AmqpException(e);
        }
    }
}