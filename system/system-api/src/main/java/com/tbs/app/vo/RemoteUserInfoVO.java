package com.tbs.app.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 默认的远程用户信息VO
 * <p>
 * 默认的远程用户信息VO，用于封装远程用户信息。
 * </p>
 *
 * @author abstergo
 */
@Data
public class RemoteUserInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 备注
     */
    private String remark;
    /**
     * 标记
     */
    private Integer flag;
    /**
     * 用户ID
     */
    private Long userId;
}
