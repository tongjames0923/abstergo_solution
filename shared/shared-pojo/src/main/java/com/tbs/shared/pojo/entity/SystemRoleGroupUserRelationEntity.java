package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import org.hibernate.annotations.Comment;
import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 角色组与用户关系实体
 *
 * <p>用于维护用户与角色组之间的关联关系，一个用户可以属于多个角色组。</p>
 *
 * @author abstergo
 * @see SystemRoleGroupEntity
 * @see SystemUserEntity
 */
@Entity
@Table(name = "system_role_group_user_relation", indexes = {
    @jakarta.persistence.Index(name = "idx_system_role_group_user_relation_role_group_id", columnList = "role_group_id"),
    @jakarta.persistence.Index(name = "idx_system_role_group_user_relation_user_id", columnList = "user_id")})
@Data
public class SystemRoleGroupUserRelationEntity implements IIdentifiable<Long> {

    /**
     * 主键ID
     */
    @Comment("主键ID（雪花算法生成）")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 角色组ID
     */
    @Comment("角色组ID（援引 sys_role_group.id）")
    @Column(name = "role_group_id")
    private Long roleGroupId;

    /**
     * 用户ID
     */
    @Comment("用户ID（援引 sys_user.id）")
    @Column(name = "user_id")
    private Long userId;
}
