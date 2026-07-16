package com.tbs.xxl.config.property;

import lombok.Data;

import java.io.Serializable;

@Data
public class XxlJobConfigForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 管理地址 必填
     */
    private String adminAddress;

    /**
     * 应用名 可空
     */
    private String appName;

    /**
     * 执行器地址 可空
     */
    private String address;

    /**
     * 执行器ip 可空
     */
    private String ip;

    /**
     * 执行器端口 可空
     */
    private Integer port;

    /**
     * 访问token 可空
     */
    private String accessToken;

    /**
     * 日志路径 可空
     */
    private String logPath;

    /**
     * 执行器日志文件保存天数 可空
     */
    private Integer logRetentionsDays;
}
