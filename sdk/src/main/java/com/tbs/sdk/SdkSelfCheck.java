package com.tbs.sdk;

import com.tbs.sdk.auth.ThirdPartyAuthCode;
import com.tbs.sdk.exception.SdkClientException;
import com.tbs.sdk.push.SsePushSubscriber;
import java.util.Locale;

/**
 * SDK自检（可运行 main）
 * <p>
 * 覆盖两处非平凡逻辑：第三方授权码SHA-256算法（对齐C#端与后端 DefaultThirdPartyGrantProvider）
 * 与SSE重连指数退避计算。用SDK及其依赖的classpath运行：
 * {@code java -cp target/classes:<依赖jar> com.tbs.sdk.SdkSelfCheck}，
 * 全部通过打印成功；任一失败以非0退出码结束。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class SdkSelfCheck {

    /**
     * SHA-256("abc") 标准测试向量（大写十六进制）
     */
    private static final String SHA256_ABC =
        "BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD";

    /**
     * 累计失败数
     */
    private static int failures = 0;

    private SdkSelfCheck() {
    }

    /**
     * 自检入口
     *
     * @param args 未使用
     */
    public static void main(String[] args) {
        checkSha256StandardVector();
        checkAuthCodeAlgorithm();
        checkAuthCodeInputValidation();
        checkReconnectBackoff();
        if (failures > 0) {
            System.err.println("[SdkSelfCheck] 存在失败项: " + failures);
            System.exit(1);
        }
        System.out.println("[SdkSelfCheck] 全部自检通过");
    }

    /**
     * 校验SHA-256实现与标准测试向量一致（证明十六进制转换与算法正确，跨语言可对齐）。
     */
    private static void checkSha256StandardVector() {
        assertEquals("SHA-256(\"abc\")大写十六进制", SHA256_ABC, ThirdPartyAuthCode.sha256Hex("abc"));
    }

    /**
     * 校验授权码等于约定算法 SHA-256(SALT:appSecret:timestamp:appId) 的小写十六进制，且确定性可复现。
     */
    private static void checkAuthCodeAlgorithm() {
        String appId = "demo-app";
        String appSecret = "s3cr3t-key";
        long timestamp = 1_700_000_000_000L;
        String baseString = ThirdPartyAuthCode.SALT + ":" + appSecret + ":" + timestamp + ":" + appId;
        String expected = ThirdPartyAuthCode.sha256Hex(baseString).toLowerCase(Locale.ROOT);
        String actual = ThirdPartyAuthCode.generate(appId, appSecret, timestamp);
        assertEquals("授权码等于约定算法重建", expected, actual);
        assertTrue("授权码为64位小写十六进制", actual.length() == 64 && actual.matches("[0-9a-f]{64}"));
        assertEquals("授权码生成确定性", actual, ThirdPartyAuthCode.generate(appId, appSecret, timestamp));
    }

    /**
     * 校验授权码生成的入参非法防护。
     */
    private static void checkAuthCodeInputValidation() {
        assertThrowsClient("appId为空应拒绝", () -> ThirdPartyAuthCode.generate(null, "x", 1L));
        assertThrowsClient("appId空白应拒绝", () -> ThirdPartyAuthCode.generate("  ", "x", 1L));
        assertThrowsClient("appSecret为null应拒绝", () -> ThirdPartyAuthCode.generate("id", null, 1L));
    }

    /**
     * 校验指数退避基础延时：base×2^attempts，封顶max，负数尝试按0处理。
     */
    private static void checkReconnectBackoff() {
        long base = 1000L;
        long max = 30000L;
        assertEquals("退避attempts=0", base, SsePushSubscriber.computeBaseReconnectDelayMs(base, max, 0));
        assertEquals("退避attempts=1", 2000L, SsePushSubscriber.computeBaseReconnectDelayMs(base, max, 1));
        assertEquals("退避attempts=2", 4000L, SsePushSubscriber.computeBaseReconnectDelayMs(base, max, 2));
        assertEquals("退避attempts=4", 16000L, SsePushSubscriber.computeBaseReconnectDelayMs(base, max, 4));
        assertEquals("退避attempts=5封顶", max, SsePushSubscriber.computeBaseReconnectDelayMs(base, max, 5));
        assertEquals("退避attempts=10封顶", max, SsePushSubscriber.computeBaseReconnectDelayMs(base, max, 10));
        assertEquals("退避负attempts按0", base, SsePushSubscriber.computeBaseReconnectDelayMs(base, max, -1));
    }

    private static void assertEquals(String name, long expected, long actual) {
        if (expected == actual) {
            System.out.println("[PASS] " + name + " = " + actual);
        } else {
            failures++;
            System.err.println("[FAIL] " + name + " 期望=" + expected + " 实际=" + actual);
        }
    }

    private static void assertEquals(String name, Object expected, Object actual) {
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        if (ok) {
            System.out.println("[PASS] " + name);
        } else {
            failures++;
            System.err.println("[FAIL] " + name + " 期望=" + expected + " 实际=" + actual);
        }
    }

    private static void assertTrue(String name, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + name);
        } else {
            failures++;
            System.err.println("[FAIL] " + name);
        }
    }

    private static void assertThrowsClient(String name, Runnable action) {
        try {
            action.run();
            failures++;
            System.err.println("[FAIL] " + name + " 未抛出SdkClientException");
        } catch (SdkClientException e) {
            System.out.println("[PASS] " + name + " -> " + e.getMessage());
        } catch (RuntimeException e) {
            failures++;
            System.err.println("[FAIL] " + name + " 抛出非预期异常: " + e.getClass().getName());
        }
    }
}
