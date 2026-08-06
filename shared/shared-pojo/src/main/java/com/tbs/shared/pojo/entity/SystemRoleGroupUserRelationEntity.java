package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "system_role_group_user_relation", indexes = {
    @javax.persistence.Index(name = "idx_system_role_group_user_relation_role_group_id", columnList = "role_group_id"),
    @javax.persistence.Index(name = "idx_system_role_group_user_relation_user_id", columnList = "user_id")})
@Data
public class SystemRoleGroupUserRelationEntity implements IIdentifiable<Long> {
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
