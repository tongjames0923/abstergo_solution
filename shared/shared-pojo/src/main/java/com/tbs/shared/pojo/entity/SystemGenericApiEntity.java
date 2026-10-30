package com.tbs.shared.pojo.entity;

import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.shared.pojo.ICommonCreateAuditable;
import com.tbs.shared.pojo.ICommonUpdateAuditable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import org.hibernate.annotations.Comment;
import lombok.*;

import jakarta.persistence.*;
import java.util.Date;

/**
 * 泛化API配置实体
 * <p>
 * 用于存储泛化API的配置信息，替代原有的字典配置方式。
 * 每个API配置包含：API标识、服务应用名、接口全限定名、方法名等信息。
 * </p>
 *
 * @author abstergo
 */
@Entity
@Table(name = "sys_generic_api", indexes = {
    @Index(name = "idx_generic_api_api_id", columnList = "api_id"),
    @Index(name = "idx_generic_api_app_name", columnList = "app_name"),
    @Index(name = "idx_generic_api_flag", columnList = "flag")
})
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@LogicDeleteEntity(activeFlagValues = {1, -1})
@Comment("泛化API配置表")
public class SystemGenericApiEntity implements IBaseEntity<Long>, ICommonCreateAuditable<Long>, ICommonUpdateAuditable<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Comment("主键ID")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * API唯一标识
     */
    @Comment("API唯一标识")
    @Column(name = "api_id", nullable = false, length = 100)
    private String apiId;

    /**
     * API名称
     */
    @Comment("API名称")
    @Column(name = "api_name", nullable = false, length = 200)
    private String apiName;

    /**
     * 服务应用名（用于服务发现）
     */
    @Comment("服务应用名（用于服务发现）")
    @Column(name = "app_name", nullable = false, length = 100)
    private String appName;

    /**
     * 接口全限定名（用于Triple HTTP调用）
     */
    @Comment("接口全限定名（用于Triple HTTP调用）")
    @Column(name = "interface_name", nullable = false, length = 300)
    private String interfaceName;

    /**
     * 方法名
     */
    @Comment("方法名")
    @Column(name = "method_name", nullable = false, length = 100)
    private String methodName;

    /**
     * 是否需要认证
     */
    @Comment("是否需要认证")
    @Column(name = "need_auth", nullable = false)
    private Boolean needAuth;

    /**
     * API版本
     */
    @Comment("API版本号")
    @Column(name = "version", length = 10)
    private Integer version;

    /**
     * 状态
     *
     * @see com.tbs.shared.enums.CommonFlagEnum
     */
    @Comment("状态标识：1-启用，-1-禁用，0-删除")
    @Column(name = "flag")
    private Integer flag;

    /**
     * 描述信息
     */
    @Comment("描述信息")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 预认证Bean列表,以逗号分隔
     */
    @Comment("预认证Bean列表（逗号分隔）")
    @Column(name = "pre_auth_beans")
    private String preAuthBeans;

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
     * 创建者
     */
    @Comment("创建者用户ID")
    @Column(name = "create_by")
    private Long createBy;

    /**
     * 更新者
     */
    @Comment("更新者用户ID")
    @Column(name = "update_by")
    private Long updateBy;
}
