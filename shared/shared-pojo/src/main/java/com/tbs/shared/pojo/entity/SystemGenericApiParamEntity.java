package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.shared.pojo.ICommonCreateAuditable;
import com.tbs.shared.pojo.ICommonUpdateAuditable;
import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.io.Serializable;
import java.util.Date;

/**
 * 泛化API入参定义实体
 * <p>
 * 一行描述一个入参，是新版泛化调用链路的<b>唯一元信息来源</b>：网关据此把 HTTP 请求里的「参数名 → 值」
 * 或「按顺序的值数组」装配成 Dubbo 泛化调用的 {@code paramTypes/args}。因为泛化调用不持有目标接口的 jar，
 * 参数签名只能靠登记，服务方无需实现任何自定义契约。
 * </p>
 * <p>
 * 与主表的关系按业务键 {@code api_id} 关联（不建外键，唯一性与合法性由 Service 层断言保证）。
 * </p>
 *
 * @author abstergo
 * @see SystemGenericApiEntity
 * @since 1.0.0
 */
@Data
@Entity
@Table(name = "sys_generic_api_param",
    indexes = {@Index(name = "idx_generic_api_param_api_id", columnList = "api_id"),
        @Index(name = "idx_generic_api_param_flag", columnList = "flag")})
@AllArgsConstructor
@NoArgsConstructor
@LogicDeleteEntity(activeFlagValues = {1, -1}, onNullIsDeleted = false)
@Comment("泛化API入参定义表")
public class SystemGenericApiParamEntity
    implements IBaseEntity<Long>, ICommonCreateAuditable<Long>, ICommonUpdateAuditable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Comment("主键ID（雪花算法生成）")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 所属泛化API的业务标识
     */
    @Comment("所属泛化API标识（援引 sys_generic_api.api_id）")
    @Column(name = "api_id", length = 100)
    private String apiId;

    /**
     * 参数在方法签名中的位置，从 0 开始连续不重复
     */
    @Comment("参数顺序，从0开始")
    @Column(name = "param_order")
    private Integer paramOrder;

    /**
     * 参数名，供命名式入参匹配；留空表示该参数只允许按位置传入
     */
    @Comment("参数名，空表示只支持按位置传参")
    @Column(name = "param_name", length = 100)
    private String paramName;

    /**
     * 声明类型：擦除后的 JVM 类名（{@code java.util.List}、{@code com.x.BarDTO}）或基本类型关键字（{@code long}），
     * 不写泛型串；集合元素类型由服务端按真实方法签名还原
     */
    @Comment("参数类型：擦除后的JVM类名或基本类型关键字，不写泛型")
    @Column(name = "param_type", length = 300)
    private String paramType;

    /**
     * 是否必填，缺省视为非必填
     * <p>
     * 列名取 {@code required_flag}：避开 {@code required} 关键字在不同方言下的引用差异。
     * </p>
     */
    @Comment("是否必填")
    @Column(name = "required_flag")
    private Boolean requiredFlag;

    /**
     * 参数语义说明，随元数据接口透出给前端
     */
    @Comment("参数说明")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 示例值（JSON 字面量文本），供前端表单预填
     */
    @Comment("示例值（JSON字面量文本）")
    @Column(name = "example", length = 500)
    private String example;

    /**
     * 状态标识
     *
     * @see com.tbs.shared.enums.CommonFlagEnum
     */
    @Comment("状态标识：1-启用，-1-禁用，0-删除")
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
