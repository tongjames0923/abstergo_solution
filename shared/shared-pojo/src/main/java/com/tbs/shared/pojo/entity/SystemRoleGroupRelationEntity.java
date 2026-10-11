package com.tbs.shared.pojo.entity;

import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.shared.pojo.ICommonCreateAuditable;
import com.tbs.shared.pojo.ICommonDeleteAuditable;
import com.tbs.shared.pojo.ICommonUpdateAuditable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 系统角色与角色组关系实体
 *
 * <p>用于维护角色（权限项）与角色组（权限组）之间的多对多关联关系。
 * 一个角色组可以包含多个角色，一个角色也可以属于多个角色组。</p>
 *
 * <p>该实体映射 sys_role_group_relation 表，支持逻辑删除和审计字段。</p>
 *
 * @author abstergo
 * @see SystemRoleEntity
 * @see SystemRoleGroupEntity
 */
@Data
@Entity
@Table(name = "sys_role_group_relation",
    indexes = {@Index(name = "idx_role_group_relation_role_id", columnList = "role_id"),
        @Index(name = "idx_role_group_relation_group_id", columnList = "group_id"),
        @Index(name = "idx_flag", columnList = "flag")})
@AllArgsConstructor
@NoArgsConstructor
@LogicDeleteEntity(activeFlagValues = {1, -1}, onNullIsDeleted = false)
@Comment("系统角色与角色组关系表")
public class SystemRoleGroupRelationEntity
    implements IBaseEntity<Long>, ICommonCreateAuditable<Long>, ICommonUpdateAuditable<Long>,
    ICommonDeleteAuditable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Comment("主键ID（雪花算法生成）")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 角色ID（权限项ID）
     */
    @Comment("角色ID（援引 sys_role.id）")
    @Column(name = "role_id")
    private Long roleId;

    /**
     * 角色组ID（权限组ID）
     */
    @Comment("角色组ID（援引 sys_role_group.id）")
    @Column(name = "group_id")
    private Long groupId;

    /**
     * 创建时间
     */
    @Comment("创建时间")
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Comment("更新时间")
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 创建者ID
     */
    @Comment("创建者用户ID")
    @Column(name = "create_by")
    private Long createBy;

    /**
     * 更新者ID
     */
    @Comment("更新者用户ID")
    @Column(name = "update_by")
    private Long updateBy;

    /**
     * 删除时间（逻辑删除）
     */
    @Comment("逻辑删除时间")
    @Column(name = "delete_time")
    private Date deleteTime;

    /**
     * 删除者ID
     */
    @Comment("删除者用户ID")
    @Column(name = "delete_by")
    private Long deleteBy;

    /**
     * 逻辑删除标志位
     *
     * @see com.tbs.shared.enums.CommonFlagEnum
     */
    @Comment("逻辑删除标志位：1-启用，-1-禁用，0-删除")
    @Column(name = "flag")
    private Integer flag;

}
