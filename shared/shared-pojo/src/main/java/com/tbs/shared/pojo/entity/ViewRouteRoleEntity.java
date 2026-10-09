package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import org.hibernate.annotations.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * 视图路由权限实体
 * <p>
 * 用于存储路由的权限配置信息，支持基于角色的路由访问控制
 * </p>
 * 
 * @author abstergo
 */
@Entity
@Table(name = "view_route_role", indexes = {
    @jakarta.persistence.Index(name = "idx_route_id", columnList = "route_id"),
    @jakarta.persistence.Index(name = "idx_role_code", columnList = "role_code")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewRouteRoleEntity implements IBaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Comment("主键ID（雪花算法生成）")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 关联的路由ID
     */
    @Comment("关联的路由ID（援引 view_route_configration.id）")
    @Column(name = "route_id", nullable = false)
    private Long routeId;

    /**
     * 权限编码
     */
    @Comment("权限编码（对应角色的 roleCode）")
    @Column(name = "role_code", length = 128, nullable = false)
    private String roleCode;

    /**
     * 状态标志（0:禁用，1:启用）
     */
    @Comment("状态标志（0:禁用，1:启用）")
    @Column(name = "flag")
    private Integer flag;

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
}
