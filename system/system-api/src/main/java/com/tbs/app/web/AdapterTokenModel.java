package com.tbs.app.web;

import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.token.model.SimpleTokenModel;
import lombok.Data;

import java.util.Date;

/**
 * 适配器令牌模型，扩展了SimpleTokenModel，增加了MAC地址和IP地址字段
 * <p>
 * 用于存储与网络相关的信息，如客户端的MAC地址和IP地址。
 * </p>
 */
@Data
public class AdapterTokenModel extends SimpleTokenModel {
    private static final long serialVersionUID = 1L;
    /**
     * 客户端MAC地址
     */
    private String macAddr;

    /**
     * 客户端IP地址
     */
    private String ipAddr;

    /**
     * 构造函数，初始化适配器令牌模型
     *
     * @param token      令牌字符串
     * @param userName   用户名
     * @param status     令牌状态
     * @param type       令牌类型
     * @param expireTime 过期时间
     * @param createTime 创建时间
     * @param createType 创建类型
     * @param macAddr    MAC地址
     * @param ipAddr     IP地址
     */
    public AdapterTokenModel(String token, String userName, TokenStatusEnum status, TokenTypeEnum type, Date expireTime,
        Date createTime, TokenCreateTypeEnum createType, String macAddr, String ipAddr, String sessionId) {
        super(token, userName, status, type, expireTime, createTime, createType, sessionId);
        this.macAddr = macAddr;
        this.ipAddr = ipAddr;
    }

    public AdapterTokenModel() {
        super();
    }
}
