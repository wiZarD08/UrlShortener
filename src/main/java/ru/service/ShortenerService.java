package ru.service;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

@Component
public class ShortenerService {
    public String create8ByteCode(String fullUrl) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // use hashing algorithm sha-256
            byte[] hash32Bytes = digest.digest(fullUrl.getBytes());
            // convert bytes to chars (6 bits - one char) and get first 8 of them
            // and get rid of bad chars '/' and '+' in the result
            return Base64.getEncoder().encodeToString(hash32Bytes).substring(0, 8)
                    .replace('/', '1').replace('+', '0');
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String changeCode(String code) {
        Random rand = new Random();
        StringBuilder builder = new StringBuilder(code);
        builder.setCharAt(rand.nextInt(code.length()), (char) (rand.nextInt(10) + '0'));
        return builder.toString();
    }
}
