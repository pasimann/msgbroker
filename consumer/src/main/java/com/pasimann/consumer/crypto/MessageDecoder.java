package com.pasimann.consumer.crypto;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.pasimann.consumer.rabbitmq.ConsumerException;
import com.pasimann.consumer.dataobjects.ApplicationData;
import com.pasimann.consumer.crypto.MessageDecoder;

public class MessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(MessageDecoder.class);

    private final static String PROPERTIES_FILENAME = "CONSUMER";
    private static ResourceBundle propertiesBundle;
    private static String secretKey;

    private final static String SPRING_CRYPTO_METHOD_AES = "AES";

    static {
       propertiesBundle = ResourceBundle.getBundle(PROPERTIES_FILENAME);

        secretKey = propertiesBundle.getString("consumer.secretkey");

        if (System.getenv("SPRING_CRYPTO_SECRET_KEY") != null) {
			secretKey = System.getenv("SPRING_CRYPTO_SECRET_KEY");
		}
    }

    private static String decodeMessage(byte[] data) throws ConsumerException {

        try {
            // base64 decode the crypted data
            byte[] base64decoded = Base64.getDecoder().decode(data);

            // Create key and cipher
            Key aesKey = new SecretKeySpec(secretKey.getBytes(), SPRING_CRYPTO_METHOD_AES);
            Cipher cipher = Cipher.getInstance(SPRING_CRYPTO_METHOD_AES);

            // decrypt the text
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            String decrypted = new String(cipher.doFinal(base64decoded), StandardCharsets.UTF_8);

            return decrypted;

        } catch (Exception e) {
            log.error("Exception when decrypting message.", e);
            throw new ConsumerException(e);
        }

    }

    public static ApplicationData getDecodedApplicationData(String json)
            throws ConsumerException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        ApplicationData result = mapper.readValue(
            json, ApplicationData.class);

        String decodedData = decodeMessage(result.getData().getBytes());

        ApplicationData decoded = new ApplicationData(decodedData, result.getDate());
        decoded.setId(result.getId());

        return decoded;
    }
}