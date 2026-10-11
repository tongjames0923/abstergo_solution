package com.tbs.sdk.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * 当前登录用户信息
 * <p>
 * 对齐后端UserInfoVO（GET /private/user/info）。后端未承诺的字段一律保留在
 * {@link #extra} 兜底，避免强类型映射失真。字段名沿用后端原拼写（avaliableRoles/
 * avaliableDepartments）以保证反序列化命中。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class UserInfoModel {

    /**
     * 用户数字id
     */
    private Long id;

    /**
     * 登录用户名
     */
    private String userName;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 账号状态码
     */
    private Integer accountStatus;

    /**
     * 账号状态文案
     */
    private String accountStatusStr;

    /**
     * 可用角色组列表（含展平前的权限组结构）
     */
    private List<RoleGroupModel> avaliableRoles;

    /**
     * 可用部门列表
     */
    private List<DepartmentModel> avaliableDepartments;

    /**
     * 当前选中部门
     */
    private DepartmentModel currentDepartment;

    /**
     * 其余未映射字段兜底
     */
    private final Map<String, Object> extra = new LinkedHashMap<>();

    /**
     * 收集未承诺字段到 {@link #extra}。
     *
     * @param key   字段名
     * @param value 字段值
     */
    @JsonAnySetter
    public void collectExtra(String key, Object value) {
        extra.put(key, value);
    }

    /**
     * 展平全部角色组内的权限编码列表（对齐前端flatPermissions逻辑）。
     * <p>鉴权编码为权限项的role字段（对齐前端getMenuRoleCodes取role）。</p>
     *
     * @return 权限编码列表，无角色组时返回空列表
     */
    public List<String> flattenPermissions() {
        List<String> permissions = new ArrayList<>();
        if (avaliableRoles == null) {
            return permissions;
        }
        for (RoleGroupModel group : avaliableRoles) {
            if (group == null || group.getPermissions() == null) {
                continue;
            }
            for (RolePermissionModel permission : group.getPermissions()) {
                if (permission != null && permission.getRole() != null && !permission.getRole().isEmpty()) {
                    permissions.add(permission.getRole());
                }
            }
        }
        return permissions;
    }

    /**
     * 角色组视图（后端SystemRoleGroupVO：group为角色组实体，permissions为权限项列表）。
     */
    @Data
    public static class RoleGroupModel {

        /**
         * 角色组信息实体
         */
        private RoleGroupInfo group;

        /**
         * 角色组下的权限项列表
         */
        private List<RolePermissionModel> permissions;

        /**
         * 角色组主键（取自group.id）。
         *
         * @return 角色组id，缺失时null
         */
        public Long getId() {
            return group == null ? null : group.getId();
        }

        /**
         * 角色组名称（取自group.roleGroupName）。
         *
         * @return 角色组名称，缺失时null
         */
        public String getName() {
            return group == null ? null : group.getRoleGroupName();
        }
    }

    /**
     * 角色组信息（后端SystemRoleGroupEntity）。
     */
    @Data
    public static class RoleGroupInfo {

        /**
         * 角色组主键
         */
        private Long id;

        /**
         * 角色组名称（后端列role_group_name）
         */
        private String roleGroupName;

        /**
         * 描述
         */
        private String description;

        /**
         * 状态标志
         */
        private Integer flag;
    }

    /**
     * 权限项（后端SystemRoleEntity，权限编码为role字段）。
     */
    @Data
    public static class RolePermissionModel {

        /**
         * 权限主键
         */
        private Long id;

        /**
         * 权限编码（唯一标识，鉴权比对用）
         */
        private String role;

        /**
         * 权限名称
         */
        private String name;

        /**
         * 权限类型
         */
        private Integer type;

        /**
         * 描述
         */
        private String description;

        /**
         * 状态标志
         */
        private Integer flag;

        /**
         * 数据范围
         */
        private Integer dataScope;
    }

    /**
     * 部门视图（字段名以后端实际返回为准，主键与名称提供兜底取值）。
     */
    @Data
    public static class DepartmentModel {

        /**
         * 主键（候选）
         */
        private Long id;

        /**
         * 部门id（候选）
         */
        private Long departmentId;

        /**
         * 部门名称（候选）
         */
        private String departmentName;

        /**
         * 名称（候选）
         */
        private String name;

        /**
         * 取主键（id与departmentId兜底）。
         *
         * @return 部门主键，均缺失时null
         */
        public Long resolvedId() {
            return id != null ? id : departmentId;
        }

        /**
         * 取名称（departmentName与name兜底）。
         *
         * @return 部门名称，均缺失时null
         */
        public String resolvedName() {
            return departmentName != null ? departmentName : name;
        }
    }
}
