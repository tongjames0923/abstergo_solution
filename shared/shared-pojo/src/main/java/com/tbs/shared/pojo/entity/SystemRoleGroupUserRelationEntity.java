package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

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
    @Column(name = "id")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 角色组ID
     */
    @Column(name = "role_group_id")
    private Long roleGroupId;

    /**
     * 用户ID
     */
    @Column(name = "user_id")
    private Long userId;
}
