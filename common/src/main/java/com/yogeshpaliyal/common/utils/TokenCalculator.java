package com.yogeshpaliyal.common.utils;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TokenCalculator {
    public static final int TOTP_DEFAULT_PERIOD = 30;
    public static final int TOTP_DEFAULT_DIGITS = 6;
    public static final int HOTP_INITIAL_COUNTER = 1;
    public static final int STEAM_DEFAULT_DIGITS = 5;

    private static final char[] STEAMCHARS = new char[]{
            '2', '3', '4', '5', '6', '7', '8', '9', 'B', 'C',
            'D', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q',
            'R', 'T', 'V', 'W', 'X', 'Y'
    };

    public enum HashAlgorithm {
        SHA1, SHA256, SHA512
    }

    public static final HashAlgorithm DEFAULT_ALGORITHM = HashAlgorithm.SHA1;

    private static byte[] generateHash(HashAlgorithm algorithm, byte[] key, byte[] data)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String algo = "Hmac" + algorithm.toString();

        Mac mac = Mac.getInstance(algo);
        mac.init(new SecretKeySpec(key, algo));

        return mac.doFinal(data);
    }

    // TODO: Rewrite tests so this compatibility wrapper can be removed
    public static int TOTP_RFC6238(byte[] secret, int period, long time, int digits, HashAlgorithm algorithm) {
        return TOTP_RFC6238(secret, period, time, digits, algorithm, 0);
    }

    public static int TOTP_RFC6238(byte[] secret, int period, long time, int digits, HashAlgorithm algorithm, int offset) {
        int fullToken = TOTP(secret, period, time, algorithm, offset);
        int div = (int) Math.pow(10, digits);

        return fullToken % div;
    }

    public static String TOTP_RFC6238(byte[] secret, int period, int digits, HashAlgorithm algorithm, int offset) {
        return Tools.formatTokenString(TOTP_RFC6238(secret, period, System.currentTimeMillis() / 1000, digits, algorithm, offset), digits);
    }

    public static String TOTP_Steam(byte[] secret, int period, int digits, HashAlgorithm algorithm, int offset) {
        int fullToken = TOTP(secret, period, System.currentTimeMillis() / 1000, algorithm, offset);

        StringBuilder tokenBuilder = new StringBuilder();

        for (int i = 0; i < digits; i++) {
            tokenBuilder.append(STEAMCHARS[fullToken % STEAMCHARS.length]);
            fullToken /= STEAMCHARS.length;
        }

        return tokenBuilder.toString();
    }

    public static String HOTP(byte[] secret, long counter, int digits, HashAlgorithm algorithm) {
        int fullToken = HOTP(secret, counter, algorithm);
        int div = (int) Math.pow(10, digits);

        return Tools.formatTokenString(fullToken % div, digits);
    }

    private static int TOTP(byte[] key, int period, long time, HashAlgorithm algorithm, int offset) {
        return HOTP(key, (time / period) + offset, algorithm);
    }

    private static int HOTP(byte[] key, long counter, HashAlgorithm algorithm) {
        int r = 0;

        try {
            byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
            byte[] hash = generateHash(algorithm, key, data);

            int offset = hash[hash.length - 1] & 0xF;

            int binary = (hash[offset] & 0x7F) << 0x18;
            binary |= (hash[offset + 1] & 0xFF) << 0x10;
            binary |= (hash[offset + 2] & 0xFF) << 0x08;
            binary |= (hash[offset + 3] & 0xFF);

            r = binary;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return r;
    }

    public static String MOTP(String PIN, String secret, long epoch, int offset) {
        String epochText = String.valueOf((epoch / 10) + offset);
        String hashText = epochText + secret + PIN;
        String otp = "";

        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(hashText.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            String hexString = new String(Hex.encodeHex(messageDigest));
            otp = hexString.substring(0, 6);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return otp;
    }
}