package com.tbs.app.web.model.dto;

import com.tbs.app.web.model.SimpleTokenModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 简单的token模型DTO
 *
 * @author abstergo
 */
@Data
public class TokenModelDto implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * token
     */
    private String token;

    /**
     * 用户名
     */
    private String userName;

    /**
     * token状态
     */
    private Integer status;

    /**
     * token类型
     */
    private Integer tokenType;

    /**
     * sessionId
     */
    private String sessionId;

    /**
     * 过期时间
     */

    private Date expireTime;

    /**
     * 创建时间
     */

    private Date createTime;

    /**
     * 创建类型
     */
    private Integer createType;

    /**
     * mac 地址
     */
    private String macAddr;

    /**
     * ip 地址
     */
    private String ipAddr;

    /**
     * 默认构造函数
     */
    public TokenModelDto() {
    }

    /**
     * 构造函数
     *
     * @param model
     */
    public TokenModelDto(SimpleTokenModel model) {
        if (model == null) {
            return;
        }
        this.token = model.getToken();
        this.userName = model.getUserName();
        this.status = model.getStatus() != null ? model.getStatus().getCode() : null;
        this.tokenType = model.getTokenType() != null ? model.getTokenType().getCode() : null;
        this.sessionId = model.getSessionId();
        this.expireTime = model.getExpireTime();
        this.createTime = model.getCreateTime();
        this.createType = model.getCreateType() != null ? model.getCreateType().getCode() : null;
        this.macAddr = model.getMacAddr();
        this.ipAddr = model.getIpAddr();
    }
}
