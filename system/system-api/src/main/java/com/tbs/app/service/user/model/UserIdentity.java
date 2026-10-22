package com.tbs.app.service.user.model;

import com.tbs.app.vo.DepartmentInfoVO;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户身份视图
 * <p>
 * 面向认证链路的扁平数据载体：一次装配返回鉴权所需的全部数据，不含权限项名称、描述、数据范围、
 * 审计字段，也不含密码。字段均为可直接序列化的基础类型或平铺集合，可安全跨进程传输。
 * </p>
 * <p>
 * 该对象表达的是「装配那一刻」的结果，不承担缓存职责：调用方每次拿到的都应是新实例。
 * </p>
 *
 * @author abstergo
 * @see com.tbs.app.service.user.IUserIdentityService 身份聚合契约
 */
@Data
public class UserIdentity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户标识
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 账号是否启用
     */
    private boolean enabled;

    /**
     * 账号是否未锁定
     */
    private boolean accountNonLocked;

    /**
     * 账号是否未过期
     */
    private boolean accountNonExpired = true;

    /**
     * 凭证是否未过期
     */
    private boolean credentialsNonExpired = true;

    /**
     * 角色组名称集合，用于构造 authority 字符串
     */
    private List<String> roleGroupNames = new ArrayList<>();

    /**
     * API 权限编码集合（权限项类型为 API_ROLE）
     */
    private Set<String> apiPermissions = new HashSet<>();

    /**
     * 菜单权限编码集合（权限项类型为 MENU_ROLE）
     */
    private Set<String> menuPermissions = new HashSet<>();

    /**
     * 部门归属，含展示字段，供用户信息接口与切部门校验复用
     */
    private List<DepartmentInfoVO> departments = new ArrayList<>();
}
