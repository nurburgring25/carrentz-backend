package dev.burikk.carrentz.engine.security;

import dev.burikk.carrentz.engine.common.Constant;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.Objects;

/**
 * @author Muhammad Irfan
 * @since 24/11/2017 16:01
 */
public class Crypt {
    public static String encrypt(Object mObject) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        IvParameterSpec mIvParameterSpec = new IvParameterSpec(Constant.Application.INIT_VECTOR.getBytes());

        SecretKeySpec mSecretKeySpec = new SecretKeySpec(Constant.Application.KEY.getBytes(), "AES");

        Cipher mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        mCipher.init(Cipher.ENCRYPT_MODE, mSecretKeySpec, mIvParameterSpec);

        byte[] mBytes = mCipher.doFinal(mObject.toString().getBytes());

        return Base64.getEncoder().encodeToString(mBytes);
    }

    @SuppressWarnings("unchecked")
    public static <T> T decrypt(String mValue, Class<T> mType) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        IvParameterSpec mIvParameterSpec = new IvParameterSpec(Constant.Application.INIT_VECTOR.getBytes());

        SecretKeySpec mSecretKeySpec = new SecretKeySpec(Constant.Application.KEY.getBytes(), "AES");

        Cipher mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        mCipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec, mIvParameterSpec);

        byte[] mBytes = mCipher.doFinal(Base64.getDecoder().decode(mValue));

        String mString = new String(mBytes);

        if (Objects.equals(BigDecimal.class, mType)) {
            return (T) new BigDecimal(mString);
        } else if (Objects.equals(String.class, mType)) {
            return (T) mString;
        } else {
            throw new RuntimeException("Unmapped type.");
        }
    }

    public static Key getKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

        keyStore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config/carrentz.jks"), Constant.Crypto.PASSWORD.toCharArray());

        return keyStore.getKey(Constant.Crypto.ALIAS, Constant.Crypto.PASSWORD.toCharArray());
    }

    public static PublicKey getPublicKey() throws CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        Certificate certificate = certificateFactory.generateCertificate(Thread.currentThread().getContextClassLoader().getResourceAsStream("config/carrentz.cer"));

        PublicKey publicKey = certificate.getPublicKey();

        return publicKey;
    }
}