package com.tbs.shared.pojo.entity;

import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.shared.pojo.ICommonCreateAuditable;
import com.tbs.shared.pojo.ICommonUpdateAuditable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import jakarta.persistence.*;
import java.util.Date;

/**
 * 第三方应用接出登记表（{@code third_party_app}）。
 * <p>
 * 记录可经「应用标识 + 授权码」方式接入系统的第三方应用，包含应用标识、密钥、登录授权类型与代理用户（执行人账号）。
 * 本表采用逻辑删除（flag），对外仅提供登记能力，核心字段一经创建不允许直接更新。
 * </p>
 */
@Data
@Builder
@Table(name = "third_party_app", indexes = {@Index(name = "idx_app_id", columnList = "app_id"),
    @Index(name = "idx_flag", columnList = "flag"), @Index(name = "idx_proxy_id", columnList = "proxy_id")})
@Entity
@AllArgsConstructor
@NoArgsConstructor
@LogicDeleteEntity(activeFlagValues = {1, 0})
@Comment("第三方应用接出登记表")
public class ThirdPartyAppEntity
    implements IBaseEntity<Long>, ICommonCreateAuditable<Long>, ICommonUpdateAuditable<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Comment("主键ID（雪花算法生成）")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    @Column(name = "id")
    private Long id;

    /**
     * 应用标识（第三方应用唯一标识）。
     * <p>登录授权时用于定位应用；未显式传入时由系统以 UUID 自动生成且全局唯一。</p>
     */
    @Comment("应用标识（第三方应用唯一标识，未传入时由系统以 UUID 生成）")
    @Column(name = "app_id", length = 64)
    private String appId;

    /**
     * 应用密钥
     * <p>不落库，用于 {@code DefaultThirdPartyGrantProvider#codeGenerate} 重建授权码并与请求方传入的 code 比对。</p>
     */
    @Transient
    private String appSecret;

    /**
     * 登录授权类型。
     * <p>对应 {@code ILoginGrantProvider#getGrantType()} 的标识（如 password / wecom / third_party），用于匹配具体授权提供方。</p>
     */
    @Comment("登录授权类型（对应授权提供方的标识，如 password / wecom / third_party）")
    @Column(name = "grant_type", length = 255)
    private String grantType;

    /**
     * 标记位
     *
     * @see com.tbs.shared.enums.CommonFlagEnum
     */
    @Comment("数据状态标志：1-启用，-1-禁用，0-删除")
    @Column(name = "flag")
    private Integer flag;

    /**
     * 代理id,关联用户表，作为该操作的执行人账号。
     */
    @Comment("代理用户ID（关联用户表，作为该操作的执行人账号）")
    @Column(name = "proxy_id")
    private Long proxyId;

    /**
     * 创建时间
     */
    @Comment("创建时间")
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 创建人
     */
    @Comment("创建者用户ID")
    @Column(name = "create_by")
    private Long createBy;

    /**
     * 更新时间
     */
    @Comment("更新时间")
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 更新人
     */
    @Comment("更新者用户ID")
    @Column(name = "update_by")
    private Long updateBy;

}
