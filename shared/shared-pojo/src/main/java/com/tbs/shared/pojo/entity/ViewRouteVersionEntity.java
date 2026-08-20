package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import org.hibernate.annotations.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 视图路由版本实体
 * <p>
 * 用于记录路由配置的版本历史，每次保存/回滚/清空分组都会产生一条新的版本记录。
 * 某分组的当前版本即该组 MAX(id) 的版本记录，历史不可变（append-only）。
 * </p>
 *
 * @author abstergo
 */
@Entity
@Table(name = "view_route_version", indexes = {
    @javax.persistence.Index(name = "idx_group", columnList = "route_group")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewRouteVersionEntity implements IBaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 版本ID
     */
    @Comment("版本ID（雪花算法生成）")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 路由分组
     */
    @Comment("路由分组（如 common、admin、develop）")
    @Column(name = "route_group", length = 64, nullable = false)
    private String routeGroup;

    /**
     * 组内版本序号（v1、v2...）
     */
    @Comment("组内版本序号（递增，如 v1、v2...）")
    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    /**
     * 版本说明
     */
    @Comment("版本说明（描述本次变更内容）")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 状态标志（0:禁用，1:启用）
     */
    @Comment("状态标志（0:禁用，1:启用）")
    @Column(name = "flag")
    private Integer flag;

    /**
     * 创建人
     */
    @Comment("创建者用户ID")
    @Column(name = "create_by")
    private Long createBy;

    /**
     * 创建时间
     */
    @Comment("创建时间")
    @Column(name = "create_time")
    private Date createTime;
}
