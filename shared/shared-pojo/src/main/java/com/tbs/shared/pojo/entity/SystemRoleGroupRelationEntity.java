package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.ICommonCreateAuditable;
import com.tbs.shared.pojo.ICommonUpdateAuditable;
import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import org.hibernate.annotations.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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
        @Index(name = "idx_role_group_relation_group_id", columnList = "group_id")})
@AllArgsConstructor
@NoArgsConstructor
public class SystemRoleGroupRelationEntity implements IIdentifiable<Long>, ICommonCreateAuditable<Long>,
    ICommonUpdateAuditable<Long>, Serializable {

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

}
