package com.tbs.common.provider;

import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;

import lombok.Getter;
import lombok.ToString;

/**
 * 来源坐标值对象。
 * <p>
 * 一次注册的「来源 + 业务分区 + 发布版本」三元组。business 承载业务分区（Dubbo group / Spring 实现类类名兜底），
 * version 承载发布/灰度版本（Dubbo version），二者从 instanceKey 字符串中解耦为一等维度，使「同一台机器上
 * 同一接口的多个 provider」凭 business/version 区分、互不碰撞。注册条目的唯一坐标 = (接口 key, 本坐标, instanceKey)。
 * </p>
 * <p>
 * business/version 允许为空串，语义为「不约束 / 由来源默认派生」；构建时 null 归一为 ""，等值基于 (source, business, version)。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Getter
@ToString
public final class SourceCoordinate {

    /**
     * 来源标识（Spring / Dubbo / 第三方）
     */
    private final ObjectSource source;

    /**
     * 业务分区，空串表示不约束/默认派生
     */
    private final String business;

    /**
     * 发布/灰度版本，空串表示不约束
     */
    private final String version;

    private SourceCoordinate(ObjectSource source, String business, String version) {
        this.source = source;
        this.business = business == null ? "" : business;
        this.version = version == null ? "" : version;
    }

    /**
     * 构建来源坐标
     *
     * @param source   来源标识，不能为null
     * @param business 业务分区，允许为null（归一为空串）
     * @param version  发布版本，允许为null（归一为空串）
     * @return 来源坐标
     */
    public static SourceCoordinate of(ObjectSource source, String business, String version) {
        AssertUtil.isTrue(source == null).i18n(CommonI18nConstant.REGISTRY_COORDINATE_SOURCE_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        return new SourceCoordinate(source, business, version);
    }

    /**
     * 仅由来源构成的坐标，用于兼容旧的「按来源定向解析」语义（business/version 均不约束）
     *
     * @param source 来源标识，不能为null
     * @return 空业务空版本坐标
     */
    public static SourceCoordinate ofSource(ObjectSource source) {
        return of(source, "", "");
    }

    /**
     * 是否声明了业务分区
     *
     * @return true 表示 business 非空
     */
    public boolean hasBusiness() {
        return !business.isEmpty();
    }

    /**
     * 是否声明了发布版本
     *
     * @return true 表示 version 非空
     */
    public boolean hasVersion() {
        return !version.isEmpty();
    }

    /**
     * 等值基于来源、业务分区与发布版本三元组
     *
     * @param o 待比较对象
     * @return true 表示坐标一致
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SourceCoordinate)) {
            return false;
        }
        var other = (SourceCoordinate) o;
        return java.util.Objects.equals(source, other.source)
            && business.equals(other.business)
            && version.equals(other.version);
    }

    /**
     * 哈希基于来源、业务分区与发布版本三元组
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(source, business, version);
    }
}
