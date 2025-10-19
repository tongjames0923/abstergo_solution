package com.tbs.common.util.base;

import org.slf4j.helpers.MessageFormatter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

/**
 * 字符串工具类
 *
 * @author abstergo
 */
public class StrUtil extends cn.hutool.core.util.StrUtil {

    private static final String EMPTY_STR = "";

    /**
     * 从输入流中读取字符串。
     *
     * @param str 输入流
     * @return 读取到的字符串
     */
    public static String readStreamAsString(InputStream str) {
        return readStreamAsString(str, Charset.defaultCharset());
    }

    /**
     * 从输入流中读取字符串。
     *
     * @param str     输入流
     * @param charset 字符编码
     * @return 读取到的字符串
     */
    public static String readStreamAsString(InputStream str, Charset charset) {
        return new BufferedReader(new InputStreamReader(str, charset)).lines()
            .collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * 对象转字符串
     *
     * @param obj 对象
     * @return 字符串
     */
    public static String toString(Object obj) {
        return toString(obj, EMPTY_STR);
    }

    /**
     * 对象转字符串
     *
     * @param obj          对象
     * @param defaultValue 默认值
     * @return 字符串
     */
    public static String toString(Object obj, String defaultValue) {
        return obj == null ? defaultValue : obj.toString();
    }

    /**
     * 格式化字符串。采用MessageFormat进行格式化。 格式 {0} {1}
     *
     * @param template 字符串模板
     * @param params   参数列表
     * @return 格式化后的字符串
     */
    public static String format(String template, Object... params) {
        return MessageFormatter.arrayFormat(template, params).getMessage();
    }

    /**
     * 将字节数组转换为十六进制字符串。
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
