package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import lombok.Data;

import javax.persistence.*;

/**
 * 系统用户部门关系
 */
@Entity
@Table(name = "system_user_department_relation", indexes = {@Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_department_id", columnList = "department_id")},
    uniqueConstraints = {@UniqueConstraint(name = "uk_user_id_department_id", columnNames = {"user_id", "department_id"})})
@Data
public class SystemUserDepartmentRelationEntity implements IIdentifiable<Long> {

    /**
     * 主键
     */
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 部门ID
     */
    @Column(name = "department_id")
    private Long departmentId;
}
