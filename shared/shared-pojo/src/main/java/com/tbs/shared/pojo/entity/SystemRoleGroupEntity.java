package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.shared.pojo.ICommonCreateAuditable;
import com.tbs.shared.pojo.ICommonDeleteAuditable;
import com.tbs.shared.pojo.ICommonUpdateAuditable;
import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 系统角色组实体
 *
 * <p>用于管理角色组（权限组），角色组是多个角色（权限项）的集合。
 * 传统角色概念（如超级管理员、普通用户）通过角色组实现。</p>
 *
 * <p>该实体映射 sys_role_group 表，支持逻辑删除和审计字段。</p>
 *
 * @author abstergo
 * @see SystemRoleEntity
 * @see SystemRoleGroupRelationEntity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sys_role_group",
    indexes = {@jakarta.persistence.Index(name = "idx_sys_role_group_flag", columnList = "flag"),
        @jakarta.persistence.Index(name = "idx_sys_role_group_role_group_name", columnList = "role_group_name")})
@LogicDeleteEntity(activeFlagValues = {1, -1})
public class SystemRoleGroupEntity
    implements IBaseEntity<Long>, ICommonCreateAuditable<Long>, ICommonUpdateAuditable<Long>,
    ICommonDeleteAuditable<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    @Column(name = "id")
    private Long id;

    /**
     * 角色组状态标识
     *
     * @see com.tbs.shared.enums.CommonFlagEnum
     */
    @Column(name = "flag")
    private Integer flag;

    /**
     * 角色组名称
     */
    @Column(name = "role_group_name")
    private String roleGroupName;

    /**
     * 角色组描述
     */
    @Column(name = "description")
    private String description;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 删除时间（逻辑删除）
     */
    @Column(name = "delete_time")
    private Date deleteTime;

    /**
     * 创建者ID
     */
    @Column(name = "create_by")
    private Long createBy;

    /**
     * 更新者ID
     */
    @Column(name = "update_by")
    private Long updateBy;

    /**
     * 删除者ID
     */
    @Column(name = "delete_by")
    private Long deleteBy;

}
