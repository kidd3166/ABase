package com.ouj.library.util;

import android.util.Base64;

import com.ouj.library.BaseApplication;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Des {

    protected static final byte[] DES_KEY = BaseApplication.DES_KEY.substring(0, 8)
            .getBytes();

    private static final byte[] iv = {1, 2, 3, 4, 5, 6, 7, 8};
    private static final IvParameterSpec zeroIv = new IvParameterSpec(iv);

    /**
     * @param source
     * @return
     * @throws Exception
     */
    public static String encode(String source) throws Exception {
        return encryptWithBase64(source, DES_KEY);
    }

    /**
     * @param code
     * @return
     * @throws Exception
     */
    public static String decode(String code) throws Exception {
        return decryptWithBase64(code, DES_KEY);
    }

    /**
     * 解密后base64输出
     *
     * @param code
     * @param key
     * @return
     * @throws Exception
     */
    public static String decryptWithBase64(String code, byte[] key)
            throws Exception {
        byte[] decryResult = Base64.decode(code, Base64.DEFAULT);
        byte[] bytes = decrypt(decryResult, key);
        return new String(bytes);
    }

    /**
     * 加密后base64输出
     *
     * @param source
     * @param key
     * @return
     * @throws Exception
     */
    public static String encryptWithBase64(String source, byte[] key)
            throws Exception {
        byte[] bytes = encrypt(source.getBytes(), key);
        String decryResult = Base64.encodeToString(bytes, Base64.DEFAULT);
        return decryResult;
    }

    public static byte[] encrypt(byte[] source, byte[] key) {
        try {
            IvParameterSpec zeroIv = new IvParameterSpec(iv);
            SecretKeySpec deskey = new SecretKeySpec(key, "DES");
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, deskey, zeroIv);
            return cipher.doFinal(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] code, byte[] key) throws Exception {
        SecretKeySpec deskey = new SecretKeySpec(key, "DES");
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, deskey, zeroIv);
        return cipher.doFinal(code);

    }
}
