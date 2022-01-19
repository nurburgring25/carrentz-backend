package dev.burikk.carrentz.engine.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Muhammad Irfan
 * @since 11/27/2016 3:21 PM
 */
public class Digest {
    public static String hexDigit(byte x) {
        StringBuilder mStringBuilder = new StringBuilder();

        char c;

        // First nibble
        c = (char) ((x >> 4) & 0xf);

        if (c > 9) {
            c = (char) ((c - 10) + 'a');
        }
        else {
            c = (char) (c + '0');
        }

        mStringBuilder.append(c);

        // Second nibble
        c = (char) (x & 0xf);

        if (c > 9) {
            c = (char) ((c - 10) + 'a');
        }
        else {
            c = (char) (c + '0');
        }

        mStringBuilder.append(c);

        return mStringBuilder.toString();
    }

    public static String SHA256(String mData) throws NoSuchAlgorithmException {
        MessageDigest mMessageDigest = MessageDigest.getInstance("SHA-256");

        byte[] mBytes = mMessageDigest.digest(mData.getBytes(StandardCharsets.UTF_8));

        StringBuilder mStringBuilder = new StringBuilder();

        for (byte aByte : mBytes) {
            mStringBuilder.append(Digest.hexDigit(aByte));
        }

        return mStringBuilder.toString();
    }

    public static String MD5(String msg) throws NoSuchAlgorithmException {
        if (msg==null) return null;

        MessageDigest algorithm = null;

        algorithm = MessageDigest.getInstance("MD5");

        byte[] content = msg.getBytes();

        if (content != null) {
            algorithm.reset();
            algorithm.update(content);

            byte[] digest = algorithm.digest();
            StringBuffer hexString = new StringBuffer();
            int digestLength = digest.length;

            for (int i = 0; i < digestLength; i++) {
                hexString.append(hexDigit(digest[i]));
            }

            return hexString.toString();
        }

        return null;
    }

    public static String MD5(String username, String password) throws NoSuchAlgorithmException {
        if (password == null) {
            return null;
        }

        return MD5(username + password);
    }
}