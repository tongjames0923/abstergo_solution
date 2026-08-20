package com.tbs.updater.config;

import com.tbs.updater.exception.UpdateException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 更新器配置类
 * <p>
 * 持有检查更新接口地址、应用标识、下载目录、部署/启动脚本等全部配置项。
 * 支持Builder模式构建，也支持从Properties对象或properties文件加载（UTF-8编码）。
 * 实例一经构建即不可变。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class UpdaterConfig {

    /**
     * 检查更新接口默认路径
     */
    public static final String DEFAULT_CHECK_PATH = "/public/app-update/check";

    /**
     * 默认超时时间（秒）
     */
    public static final int DEFAULT_TIMEOUT_SECONDS = 30;

    /**
     * properties键名：服务端主机
     */
    public static final String KEY_SERVER_HOST = "server.host";

    /**
     * properties键名：服务端端口
     */
    public static final String KEY_SERVER_PORT = "server.port";

    /**
     * properties键名：检查更新接口路径
     */
    public static final String KEY_SERVER_CHECK_PATH = "server.checkPath";

    /**
     * properties键名：应用唯一标识
     */
    public static final String KEY_APP_KEY = "app.appKey";

    /**
     * properties键名：平台标识
     */
    public static final String KEY_APP_PLATFORM = "app.platform";

    /**
     * properties键名：当前应用版本号
     */
    public static final String KEY_APP_CURRENT_VERSION_CODE = "app.currentVersionCode";

    /**
     * properties键名：更新包下载目录
     */
    public static final String KEY_LOCAL_DOWNLOAD_DIR = "local.downloadDir";

    /**
     * properties键名：部署脚本路径
     */
    public static final String KEY_LOCAL_DEPLOY_SCRIPT = "local.deployScript";

    /**
     * properties键名：启动脚本路径
     */
    public static final String KEY_LOCAL_START_SCRIPT = "local.startScript";

    /**
     * properties键名：网络超时时间（秒）
     */
    public static final String KEY_UPDATE_TIMEOUT_SECONDS = "update.timeoutSeconds";

    /**
     * properties键名：下载失败时是否尝试其余地址
     */
    public static final String KEY_UPDATE_TRY_ALL_URLS = "update.tryAllUrls";

    /**
     * 本地已安装版本文件名
     * <p>
     * 更新成功后写入到 JVM 工作目录（user.dir）下，内容为最新 versionCode。
     * 下次启动时读取，作为 currentVersionCode 使用。
     * 跟随 JAR 包所在目录，不受系统临时目录清理影响。
     * </p>
     */
    public static final String LOCAL_VERSION_FILE = ".installed-version";

    /**
     * 服务端主机（IP或域名，可携带http/https协议头）
     */
    private final String serverHost;

    /**
     * 服务端端口，小于等于0时表示URL中不带端口
     */
    private final int serverPort;

    /**
     * 检查更新接口路径
     */
    private final String checkPath;

    /**
     * 应用唯一标识
     */
    private final String appKey;

    /**
     * 平台标识（如android、ios）
     */
    private final String platform;

    /**
     * 当前应用版本号
     */
    private final int currentVersionCode;

    /**
     * 更新包下载目录
     */
    private final String downloadDir;

    /**
     * 部署脚本路径，可为空表示跳过部署步骤
     */
    private final String deployScript;

    /**
     * 启动脚本路径，可为空表示跳过启动步骤
     */
    private final String startScript;

    /**
     * 网络超时时间（秒）
     */
    private final int timeoutSeconds;

    /**
     * 下载失败时是否依次尝试其余下载地址
     */
    private final boolean tryAllUrls;

    /**
     * 私有构造，仅允许通过Builder构建
     *
     * @param builder 配置构建器
     */
    private UpdaterConfig(Builder builder) {
        this.serverHost = builder.serverHost;
        this.serverPort = builder.serverPort;
        this.checkPath = builder.checkPath;
        this.appKey = builder.appKey;
        this.platform = builder.platform;
        this.currentVersionCode = builder.currentVersionCode;
        this.downloadDir = builder.downloadDir;
        this.deployScript = builder.deployScript;
        this.startScript = builder.startScript;
        this.timeoutSeconds = builder.timeoutSeconds;
        this.tryAllUrls = builder.tryAllUrls;
    }

    /**
     * 从Properties对象加载配置
     * <p>
     * 键名约定见各KEY_*常量，未提供的可选项使用默认值。
     * </p>
     *
     * @param props 配置属性对象
     * @return 更新器配置实例
     * @throws UpdateException 配置缺失或数值非法时抛出CONFIG_ERROR
     */
    public static UpdaterConfig fromProperties(Properties props) throws UpdateException {
        if (props == null) {
            throw new UpdateException(UpdateException.ErrorType.CONFIG_ERROR, "配置Properties不能为空");
        }
        Builder builder = new Builder()
                .serverHost(trimToNull(props.getProperty(KEY_SERVER_HOST)))
                .serverPort(parseIntProperty(props, KEY_SERVER_PORT, 0))
                .appKey(trimToNull(props.getProperty(KEY_APP_KEY)))
                .platform(trimToNull(props.getProperty(KEY_APP_PLATFORM)))
                .currentVersionCode(parseIntProperty(props, KEY_APP_CURRENT_VERSION_CODE, 0))
                .deployScript(trimToNull(props.getProperty(KEY_LOCAL_DEPLOY_SCRIPT)))
                .startScript(trimToNull(props.getProperty(KEY_LOCAL_START_SCRIPT)))
                .timeoutSeconds(parseIntProperty(props, KEY_UPDATE_TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS));
        /*
         * downloadDir未配置时不覆盖Builder默认值（系统临时目录/updates）
         */
        String downloadDir = trimToNull(props.getProperty(KEY_LOCAL_DOWNLOAD_DIR));
        if (downloadDir != null) {
            builder.downloadDir(downloadDir);
        }
        String checkPath = trimToNull(props.getProperty(KEY_SERVER_CHECK_PATH));
        if (checkPath != null) {
            builder.checkPath(checkPath);
        }
        String tryAllUrls = trimToNull(props.getProperty(KEY_UPDATE_TRY_ALL_URLS));
        if (tryAllUrls != null) {
            builder.tryAllUrls(Boolean.parseBoolean(tryAllUrls));
        }
        return builder.build();
    }

    /**
     * 从properties文件加载配置（按UTF-8编码读取，支持中文值）
     *
     * @param path properties文件路径
     * @return 更新器配置实例
     * @throws UpdateException 文件不存在、读取失败或配置非法时抛出CONFIG_ERROR
     */
    public static UpdaterConfig fromFile(String path) throws UpdateException {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            throw new UpdateException(UpdateException.ErrorType.CONFIG_ERROR, "配置文件不存在: " + path);
        }
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (IOException e) {
            throw new UpdateException(UpdateException.ErrorType.CONFIG_ERROR, "读取配置文件失败: " + path, e);
        }
        return fromProperties(props);
    }

    /**
     * 拼接完整的检查更新接口URL
     * <p>
     * serverHost未携带协议头时默认补http://，serverPort小于等于0时不拼接端口。
     * </p>
     *
     * @return 检查更新接口完整URL
     */
    public String buildCheckUrl() {
        String base;
        if (serverHost.startsWith("http://") || serverHost.startsWith("https://")) {
            base = serverHost;
        } else {
            base = "http://" + serverHost;
        }
        if (serverPort > 0) {
            base = base + ":" + serverPort;
        }
        String path = checkPath.startsWith("/") ? checkPath : "/" + checkPath;
        return base + path;
    }

    /**
     * 获取版本文件所在目录（JVM 工作目录，即 JAR 包启动目录）
     *
     * @return 版本文件目录的绝对路径
     */
    public static String getLocalVersionDir() {
        return System.getProperty("user.dir");
    }

    /**
     * 读取本地已安装的版本号
     * <p>
     * 从 JVM 工作目录（user.dir）下的 {@link #LOCAL_VERSION_FILE} 文件中
     * 读取上次更新成功写入的 versionCode。
     * 文件不存在或内容非法时返回 -1，表示无本地版本记录。
     * </p>
     *
     * @return 本地记录的版本号，无记录时返回 -1
     */
    public static int readLocalVersion() {
        String dir = getLocalVersionDir();
        if (dir == null || dir.trim().isEmpty()) {
            return -1;
        }
        File versionFile = new File(dir, LOCAL_VERSION_FILE);
        if (!versionFile.exists() || !versionFile.isFile()) {
            return -1;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(versionFile), StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                return Integer.parseInt(line.trim());
            }
        } catch (IOException | NumberFormatException e) {
            /*
             * 文件损坏或内容非法，忽略并返回 -1
             */
        }
        return -1;
    }

    /**
     * 校验必填配置项
     *
     * @throws UpdateException 必填项缺失时抛出CONFIG_ERROR
     */
    public void validate() throws UpdateException {
        requireNonBlank(serverHost, KEY_SERVER_HOST);
        requireNonBlank(appKey, KEY_APP_KEY);
        requireNonBlank(platform, KEY_APP_PLATFORM);
    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getCheckPath() {
        return checkPath;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getPlatform() {
        return platform;
    }

    public int getCurrentVersionCode() {
        return currentVersionCode;
    }

    public String getDownloadDir() {
        return downloadDir;
    }

    public String getDeployScript() {
        return deployScript;
    }

    public String getStartScript() {
        return startScript;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public boolean isTryAllUrls() {
        return tryAllUrls;
    }

    /**
     * 解析整数类型属性值
     *
     * @param props        配置属性对象
     * @param key          属性键名
     * @param defaultValue 属性缺失时的默认值
     * @return 解析后的整数值
     * @throws UpdateException 数值格式非法时抛出CONFIG_ERROR
     */
    private static int parseIntProperty(Properties props, String key, int defaultValue) throws UpdateException {
        String value = trimToNull(props.getProperty(key));
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new UpdateException(UpdateException.ErrorType.CONFIG_ERROR,
                    "配置项" + key + "必须为整数, 当前值: " + value, e);
        }
    }

    /**
     * 断言配置项非空白
     *
     * @param value 配置值
     * @param key   配置键名（用于错误提示）
     * @throws UpdateException 值为空白时抛出CONFIG_ERROR
     */
    private static void requireNonBlank(String value, String key) throws UpdateException {
        if (value == null || value.trim().isEmpty()) {
            throw new UpdateException(UpdateException.ErrorType.CONFIG_ERROR, "缺少必填配置项: " + key);
        }
    }

    /**
     * 去除字符串首尾空白，空串转为null
     *
     * @param value 原始字符串
     * @return 处理后的字符串
     */
    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 更新器配置构建器
     *
     * @author abstergo
     * @since 1.0.0
     */
    public static class Builder {

        private String serverHost;
        private int serverPort;
        private String checkPath = DEFAULT_CHECK_PATH;
        private String appKey;
        private String platform;
        private int currentVersionCode;
        private String downloadDir = new File(System.getProperty("java.io.tmpdir"), "updates").getAbsolutePath();
        private String deployScript;
        private String startScript;
        private int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
        private boolean tryAllUrls = true;

        public Builder serverHost(String serverHost) {
            this.serverHost = serverHost;
            return this;
        }

        public Builder serverPort(int serverPort) {
            this.serverPort = serverPort;
            return this;
        }

        public Builder checkPath(String checkPath) {
            this.checkPath = checkPath;
            return this;
        }

        public Builder appKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        public Builder platform(String platform) {
            this.platform = platform;
            return this;
        }

        public Builder currentVersionCode(int currentVersionCode) {
            this.currentVersionCode = currentVersionCode;
            return this;
        }

        public Builder downloadDir(String downloadDir) {
            if (downloadDir != null) {
                this.downloadDir = downloadDir;
            }
            return this;
        }

        public Builder deployScript(String deployScript) {
            this.deployScript = deployScript;
            return this;
        }

        public Builder startScript(String startScript) {
            this.startScript = startScript;
            return this;
        }

        public Builder timeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public Builder tryAllUrls(boolean tryAllUrls) {
            this.tryAllUrls = tryAllUrls;
            return this;
        }

        /**
         * 构建不可变的配置实例
         *
         * @return 更新器配置实例
         */
        public UpdaterConfig build() {
            return new UpdaterConfig(this);
        }
    }
}
