package com.tbs.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 角色VO（View Object）
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 权限编码
     */
    private String role;

    /**
     * 权限名称
     */
    private String roleName;

    /**
     * 权限类型
     */
    private Integer type;
    /**
     * 权限状态
     */
    private Integer flag;
}
