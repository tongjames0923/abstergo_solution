package com.tbs.common.provider.impl;

import java.util.Objects;
import java.util.function.Supplier;

import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.provider.RegistryEntry;
import com.tbs.common.provider.SourceCoordinate;

/**
 * 注册条目默认实现。
 * <p>
 * 以 (key, coordinate, instanceKey) 三元组为唯一身份实现 equals/hashCode，供注册中心在
 * {@code replaceEntriesBySource} 摘除旧条目、以及在多值桶内区分同机不同 business/version 的实例。
 * 实例可为直接传入值，也可为 {@link Supplier} 懒解析：懒解析条目首次 {@link #resolve()} 时构建并缓存，
 * 后续复用，避免重复订阅/建连等昂贵操作。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DefaultRegistryEntry implements RegistryEntry {

    /**
     * 接口类型（注册桶键）
     */
    private final Class<?> key;

    /**
     * 来源坐标
     */
    private final SourceCoordinate coordinate;

    /**
     * 实例物理定位
     */
    private final String instanceKey;

    /**
     * 同坐标内优先级
     */
    private final int order;

    /**
     * 实例供给器，懒解析来源；直接实例条目为其包装的常量供给
     */
    private final Supplier<Object> supplier;

    /**
     * 是否作为枚举视图条目呈现
     */
    private final boolean enumerable;

    /**
     * 是否作为路由目标条目参与择一解析
     */
    private final boolean routable;

    /**
     * 已解析实例缓存，volatile 保证懒解析结果对并发读可见
     */
    private volatile Object resolved;

    private DefaultRegistryEntry(Class<?> key, SourceCoordinate coordinate, String instanceKey, int order,
        Supplier<Object> supplier, boolean enumerable, boolean routable) {
        this.key = key;
        this.coordinate = coordinate;
        this.instanceKey = instanceKey;
        this.order = order;
        this.supplier = supplier;
        this.enumerable = enumerable;
        this.routable = routable;
    }

    /**
     * 构建注册条目，快速失败校验三元组身份非空
     *
     * @param key         接口类型，不能为null
     * @param coordinate  来源坐标，不能为null
     * @param instanceKey 实例物理定位，不能为null
     * @param order       同坐标内优先级
     * @param supplier    实例供给器，不能为null
     * @return 注册条目
     */
    public static DefaultRegistryEntry of(Class<?> key, SourceCoordinate coordinate, String instanceKey, int order,
        Supplier<Object> supplier) {
        return of(key, coordinate, instanceKey, order, supplier, true, true);
    }

    /**
     * 构建注册条目并显式声明角色（枚举视图 / 路由目标）
     * <p>
     * 供来源桥接区分「逐实例枚举条目」与「集群路由条目」：二者身份三元组不同，在同一接口桶内并存。
     * </p>
     *
     * @param key         接口类型，不能为null
     * @param coordinate  来源坐标，不能为null
     * @param instanceKey 实例物理定位，不能为null
     * @param order       同坐标内优先级
     * @param supplier    实例供给器，不能为null
     * @param enumerable  是否作为枚举视图条目
     * @param routable    是否作为路由目标条目
     * @return 注册条目
     */
    public static DefaultRegistryEntry of(Class<?> key, SourceCoordinate coordinate, String instanceKey, int order,
        Supplier<Object> supplier, boolean enumerable, boolean routable) {
        AssertUtil.isTrue(key == null).i18n(CommonI18nConstant.REGISTRY_ENTRY_KEY_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        AssertUtil.isTrue(coordinate == null).i18n(CommonI18nConstant.REGISTRY_ENTRY_COORDINATE_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        AssertUtil.isTrue(instanceKey == null).i18n(CommonI18nConstant.REGISTRY_ENTRY_INSTANCE_KEY_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        AssertUtil.isTrue(supplier == null).i18n(CommonI18nConstant.REGISTRY_ENTRY_SUPPLIER_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        return new DefaultRegistryEntry(key, coordinate, instanceKey, order, supplier, enumerable, routable);
    }

    /**
     * 构建持有固定实例的注册条目
     *
     * @param key         接口类型，不能为null
     * @param coordinate  来源坐标，不能为null
     * @param instanceKey 实例物理定位，不能为null
     * @param order       同坐标内优先级
     * @param instance    实例本体，不能为null
     * @return 注册条目
     */
    public static DefaultRegistryEntry ofInstance(Class<?> key, SourceCoordinate coordinate, String instanceKey,
        int order, Object instance) {
        return ofInstance(key, coordinate, instanceKey, order, instance, true, true);
    }

    /**
     * 构建持有固定实例的注册条目并显式声明角色
     *
     * @param key         接口类型，不能为null
     * @param coordinate  来源坐标，不能为null
     * @param instanceKey 实例物理定位，不能为null
     * @param order       同坐标内优先级
     * @param instance    实例本体，不能为null
     * @param enumerable  是否作为枚举视图条目
     * @param routable    是否作为路由目标条目
     * @return 注册条目
     */
    public static DefaultRegistryEntry ofInstance(Class<?> key, SourceCoordinate coordinate, String instanceKey,
        int order, Object instance, boolean enumerable, boolean routable) {
        AssertUtil.isTrue(instance == null).i18n(CommonI18nConstant.REGISTRY_ENTRY_SUPPLIER_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        return of(key, coordinate, instanceKey, order, () -> instance, enumerable, routable);
    }

    /**
     * 是否作为枚举视图条目呈现
     *
     * @return true 表示该条目应出现在多实现枚举结果中
     */
    @Override
    public boolean enumerable() {
        return enumerable;
    }

    /**
     * 是否作为路由目标条目参与择一解析
     *
     * @return true 表示该条目可被择一路由命中
     */
    @Override
    public boolean routable() {
        return routable;
    }

    /**
     * 获取接口类型
     *
     * @return 目标接口 Class
     */
    @Override
    public Class<?> key() {
        return key;
    }

    /**
     * 获取来源坐标
     *
     * @return 来源坐标
     */
    @Override
    public SourceCoordinate coordinate() {
        return coordinate;
    }

    /**
     * 获取实例物理定位
     *
     * @return 实例标识
     */
    @Override
    public String instanceKey() {
        return instanceKey;
    }

    /**
     * 获取同坐标内优先级
     *
     * @return 优先级
     */
    @Override
    public int order() {
        return order;
    }

    /**
     * 解析为可调用实例
     * <p>
     * 双检锁懒解析：首次调用触发供给器构建实例并缓存，后续并发读直接命中缓存，避免重复建连/订阅。
     * </p>
     *
     * @return 实例本体
     */
    @Override
    public Object resolve() {
        Object local = resolved;
        if (local == null) {
            synchronized (this) {
                local = resolved;
                if (local == null) {
                    local = supplier.get();
                    AssertUtil.isTrue(local == null).i18n(CommonI18nConstant.REGISTRY_ENTRY_SUPPLIER_REQUIRED)
                        .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
                    resolved = local;
                }
            }
        }
        return local;
    }

    /**
     * 等值基于唯一身份三元组 (key, coordinate, instanceKey)，与 order/实例无关
     *
     * @param o 待比较对象
     * @return true 表示同一条目身份
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultRegistryEntry)) {
            return false;
        }
        var other = (DefaultRegistryEntry) o;
        return Objects.equals(key, other.key)
            && Objects.equals(coordinate, other.coordinate)
            && Objects.equals(instanceKey, other.instanceKey);
    }

    /**
     * 哈希基于唯一身份三元组
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(key, coordinate, instanceKey);
    }

    /**
     * 输出条目身份摘要，便于日志排查
     *
     * @return 可读字符串
     */
    @Override
    public String toString() {
        return "RegistryEntry{key=" + key.getName() + ", coordinate=" + coordinate + ", instanceKey=" + instanceKey + '}';
    }
}
