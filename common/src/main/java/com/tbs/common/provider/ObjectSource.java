package com.tbs.common.provider;

import java.util.Objects;

import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;

/**
 * 对象实例来源标识（值对象）。
 * <p>
 * 由封闭枚举改为不可变值对象：内置 {@link #SPRING}/{@link #DUBBO} 常量给现有能力开箱即用，
 * 第三方经 {@link #of(String, String)} 造新来源标识，兑现「兼容 Nacos 之外的后端」。
 * 等值语义基于 id，替代原枚举的 {@code ==}；保留 isSpring/isDubbo 便捷判定以兼容存量调用点。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class ObjectSource {

    /**
     * Spring 容器来源：容器内普通 Bean 或经 @DubboReference 注册进容器的引用 Bean
     */
    public static final ObjectSource SPRING = of("spring", "Spring 容器本地 Bean");

    /**
     * Dubbo 来源：经注册中心发现的服务提供者实例
     */
    public static final ObjectSource DUBBO = of("dubbo", "Dubbo/Nacos 远程实例");

    /**
     * 全局唯一来源标识
     */
    private final String id;

    /**
     * 来源人类可读描述（仅日志/展示）
     */
    private final String description;

    private ObjectSource(String id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * 构建来源标识，供第三方声明新来源
     *
     * @param id          来源唯一标识，不能为空白
     * @param description 来源描述，可为空
     * @return 来源标识；相同 id 视为同一来源（equals 基于 id）
     */
    public static ObjectSource of(String id, String description) {
        AssertUtil.isTrue(StrUtil.isEmpty(id)).i18n(CommonI18nConstant.REGISTRY_SOURCE_ID_BLANK)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        return new ObjectSource(id, description);
    }

    /**
     * 获取来源唯一标识
     *
     * @return 来源 id
     */
    public String id() {
        return id;
    }

    /**
     * 获取来源描述
     *
     * @return 来源描述
     */
    public String description() {
        return description;
    }

    /**
     * 是否为 Spring 来源（兼容便捷方法，替代原枚举 == 判定）
     *
     * @return true 表示 Spring 来源
     */
    public boolean isSpring() {
        return SPRING.equals(this);
    }

    /**
     * 是否为 Dubbo 来源（兼容便捷方法）
     *
     * @return true 表示 Dubbo 来源
     */
    public boolean isDubbo() {
        return DUBBO.equals(this);
    }

    /**
     * 等值基于 id，相同 id 视为同一来源
     *
     * @param o 待比较对象
     * @return true 表示来源标识一致
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ObjectSource)) {
            return false;
        }
        return Objects.equals(id, ((ObjectSource) o).id);
    }

    /**
     * 哈希基于 id，与 equals 一致
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    /**
     * 返回来源 id，替代原枚举 name 的日志形态
     *
     * @return 来源 id
     */
    @Override
    public String toString() {
        return id;
    }
}
