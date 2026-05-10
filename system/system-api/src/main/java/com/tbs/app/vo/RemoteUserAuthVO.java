package com.tbs.app.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author abstergo
 */
@Data
public class RemoteUserAuthVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 访问令牌类型
     */
    private Integer tokenTypeCode;

    /**
     * 用户名
     */
    private String userName;
    /**
     * 访问令牌
     */
    private String token;
    /**
     * 角色列表
     */
    private List<String> roles;
    /**
     * 令牌过期时间
     */

    private Date expireTime;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 状态码
     */
    private Integer statusCode;
}
