package com.tbs.sdk.model;

/**
 * 客户端平台常量
 * <p>
 * 对齐后端升级检查入参platform约定，客户端上报当前运行平台以匹配对应安装包。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class AppPlatform {

    /**
     * 安卓
     */
    public static final String ANDROID = "ANDROID";

    /**
     * iOS
     */
    public static final String IOS = "IOS";

    /**
     * Web
     */
    public static final String WEB = "WEB";

    /**
     * Windows
     */
    public static final String WINDOWS = "WINDOWS";

    /**
     * macOS
     */
    public static final String MACOS = "MACOS";

    /**
     * Linux
     */
    public static final String LINUX = "LINUX";

    private AppPlatform() {
    }
}
