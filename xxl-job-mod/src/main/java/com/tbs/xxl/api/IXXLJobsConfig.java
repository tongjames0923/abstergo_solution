package com.tbs.xxl.api;

/**
 * xxl job 配置
 *
 * @author tongj
 */
public interface IXXLJobsConfig {
    /**
     * 管理地址 必填
     *
     * @return
     */
    String adminAddress();

    /**
     * 应用名 可空
     *
     * @return
     */
    String appName();

    /**
     * 执行器地址 可空
     *
     * @return
     */
    String address();

    /**
     * 执行器ip 可空
     *
     * @return
     */
    String ip();

    /**
     * 执行器ip 端口 可空
     *
     * @return
     */
    Integer port();

    /**
     * 访问token 可空
     *
     * @return
     */
    String accessToken();

    /**
     * 日志路径 可空
     *
     * @return
     */
    String logPath();

    /**
     * 执行器日志文件保存天数  可空
     *
     * @return
     */
    Integer logRetentionsDays();
}
