package com.tbs.common.util.encrypt;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ExceptionUtil;
import com.tbs.common.util.StrUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 不可逆加密工具类
 *
 * @author tbs
 */
public class EncryptUitl {

    private static final String SHA128 = "SHA-128";
    private static final String SHA256 = "SHA-256";
    private static final String SHA512 = "SHA-512";

    /**
     * md5 加密
     *
     * @param uuid
     * @param salt
     * @param instance
     * @return
     */
    private static byte[] md5(String uuid, String salt, String instance) {
        String uuidWithSalt = uuid + salt;
        byte[] hash = uuidWithSalt.getBytes();
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(instance);
        } catch (NoSuchAlgorithmException e) {
            AssertUtil.throwError(() -> ExceptionUtil.newException(e, "获取加密实例异常"));
        }
        return digest.digest(hash);
    }

    /**
     * 使用 SHA-256 算法对输入数据进行哈希加密，并结合随机盐值增加安全性。
     *
     * @param data 待加密的数据
     * @return SHA-256 加密后的十六进制字符串结果，若输入为空则返回 null
     */
    public static final String hash256Encrypt(String data, String salt) {
        if (StrUtil.isEmpty(data)) {
            return null;
        }
        byte[] bytes = md5(data, salt, SHA256);
        return StrUtil.bytesToHex(bytes);
    }

    /**
     * 使用 SHA-1（128位）算法对输入数据进行哈希加密，并结合随机盐值增加安全性。
     *
     * @param data 待加密的数据
     * @return SHA-1 加密后的十六进制字符串结果，若输入为空则返回 null
     */
    public static final String hash128Encrypt(String data, String salt) {
        if (StrUtil.isEmpty(data)) {
            return null;
        }
        byte[] bytes = md5(data, salt, SHA128);
        return StrUtil.bytesToHex(bytes);
    }

    /**
     * 使用 SHA-512 算法对输入数据进行哈希加密，并结合随机盐值增加安全性。 注意：当前方法体中调用了 md5(data, salt, algorithm)，algorithm 应传入 SHA512 常量。
     *
     * @param data 待加密的数据
     * @return SHA-512 加密后的十六进制字符串结果，若输入为空则返回 null
     */
    public static final String hash512Encrypt(String data, String salt) {
        if (StrUtil.isEmpty(data)) {
            return null;
        }
        byte[] bytes = md5(data, salt, SHA512); // 已更正为 SHA512
        return StrUtil.bytesToHex(bytes);
    }

}
