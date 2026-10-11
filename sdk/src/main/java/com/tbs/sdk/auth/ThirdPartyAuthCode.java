package com.tbs.sdk.auth;

import com.tbs.sdk.exception.SdkClientException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * 第三方应用授权码生成器
 * <p>
 * 对齐后端DefaultThirdPartyGrantProvider.codeGenerate：授权码为
 * SHA-256(SALT:appSecret:timestamp:appId) 的小写十六进制串，服务端以相同算法重建比对，
 * 时间戳有效窗口±3分钟。SALT为与服务端约定的固定常量，禁止修改。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class ThirdPartyAuthCode {

    /**
     * 授权码生成盐值（与后端固定常量一致，禁止修改）
     */
    public static final String SALT = "cad39065478d1e81fc528f9a6be211a5";

    private ThirdPartyAuthCode() {
    }

    /**
     * 生成第三方登录授权码。
     *
     * @param appId     第三方应用标识（必填）
     * @param appSecret 第三方应用密钥（必填）
     * @param timestamp 毫秒时间戳（与请求体timestamp一致）
     * @return 64位小写十六进制授权码
     * @throws SdkClientException appId/appSecret非法或JVM不支持SHA-256时抛出
     */
    public static String generate(String appId, String appSecret, long timestamp) {
        if (appId == null || appId.trim().isEmpty()) {
            throw new SdkClientException("第三方应用标识appId不能为空");
        }
        if (appSecret == null) {
            throw new SdkClientException("第三方应用密钥appSecret不能为空");
        }
        String baseString = SALT + ":" + appSecret + ":" + timestamp + ":" + appId;
        return toHex(sha256(baseString), false);
    }

    /**
     * 计算字符串的SHA-256大写十六进制值（独立暴露便于校验与自检比对）。
     *
     * @param input 输入字符串（null按空串处理）
     * @return 64位大写十六进制摘要
     * @throws SdkClientException JVM不支持SHA-256时抛出
     */
    public static String sha256Hex(String input) {
        return toHex(sha256(input == null ? "" : input), true);
    }

    /**
     * 计算输入串的SHA-256字节摘要。
     *
     * @param input 输入字符串
     * @return 32字节摘要
     */
    private static byte[] sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new SdkClientException("当前JVM不支持SHA-256算法", e);
        }
    }

    /**
     * 字节数组转十六进制字符串。
     *
     * @param bytes 字节数组
     * @param upper 是否大写
     * @return 十六进制串
     */
    private static String toHex(byte[] bytes, boolean upper) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(Character.forDigit((b >> 4) & 0xF, 16));
            builder.append(Character.forDigit(b & 0xF, 16));
        }
        return upper ? builder.toString().toUpperCase(Locale.ROOT) : builder.toString();
    }
}
