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
 * 视图路由元数据实体
 * <p>
 * 用于存储路由的元数据信息，如标题、图标、面包屑配置等
 * </p>
 * 
 * @author abstergo
 */
@Entity
@Table(name = "view_route_meta", indexes = {
    @javax.persistence.Index(name = "idx_route_id", columnList = "route_id")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewRouteMetaEntity implements IBaseEntity<Long> {

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
     * 路由分组
     */
    @Comment("路由分组（与路由配置的 route_group 对应）")
    @Column(name = "route_group", length = 64)
    private String routeGroup;

    /**
     * 路由标题
     */
    @Comment("路由标题（显示在菜单和面包屑中）")
    @Column(name = "title", length = 128)
    private String title;

    /**
     * 路由图标
     */
    @Comment("路由图标（菜单图标名称）")
    @Column(name = "icon", length = 128)
    private String icon;

    /**
     * 是否显示面包屑（0:否，1:是）
     */
    @Comment("是否显示面包屑（0:否，1:是）")
    @Column(name = "breadcrumb")
    private Integer breadcrumb;

    /**
     * 激活菜单路径
     */
    @Comment("激活菜单路径（高亮指定的菜单项）")
    @Column(name = "active_menu", length = 255)
    private String activeMenu;

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
