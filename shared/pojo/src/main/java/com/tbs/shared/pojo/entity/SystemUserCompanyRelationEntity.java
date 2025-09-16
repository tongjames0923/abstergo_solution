package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IIdentifiable;
import lombok.Data;

import javax.persistence.*;

/**
 * 系统用户公司关系
 */
@Entity
@Table(name = "system_user_company_relation", indexes = {@Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_company_id", columnList = "company_id")},
    uniqueConstraints = {@UniqueConstraint(name = "uk_user_id_company_id", columnNames = {"user_id", "company_id"})})
@Data
public class SystemUserCompanyRelationEntity implements IIdentifiable<Long> {

    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 公司ID
     */
    @Column(name = "company_id")
    private Long companyId;
}
