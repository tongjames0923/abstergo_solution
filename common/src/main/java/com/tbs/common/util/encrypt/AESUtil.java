package com.tbs.common.util.encrypt;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES加密工具类
 *
 * @author abstergo
 */
public class AESUtil {

    /**
     * AES 加密算法
     */
    private static final String AES = "AES";
    /**
     * AES 加密模式
     **/
    private static final String AES_CIPHER = "AES/CBC/PKCS5Padding";

    /**
     * 加密
     *
     * @param key  密钥
     * @param data 待加密数据
     * @return 加密后的数据
     * @throws Exception
     */
    public static String encrypt(String key, String data) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES);
        IvParameterSpec iv = new IvParameterSpec(key.getBytes(StandardCharsets.UTF_8), 0, 16);
        Cipher cipher = Cipher.getInstance(AES_CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * 解密
     *
     * @param key
     * @param encryptedData
     * @return
     * @throws Exception
     */
    public static String decrypt(String key, String encryptedData) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES);
        IvParameterSpec iv = new IvParameterSpec(key.getBytes(StandardCharsets.UTF_8), 0, 16);
        Cipher cipher = Cipher.getInstance(AES_CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        byte[] original = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(original, StandardCharsets.UTF_8);
    }
}
