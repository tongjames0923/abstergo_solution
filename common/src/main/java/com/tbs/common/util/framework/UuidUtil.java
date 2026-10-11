package com.tbs.common.util.framework;

import com.tbs.common.config.property.UuidProperty;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.uuid.IUuidGenerator;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * UUID统一门面
 * <p>
 * 静态持有器模式：实例由Spring容器创建（构造器赋静态引用），静态调用方经{@link #getInstance()}访问。
 * 针对"Bean实例化之前被调用"的初始化顺序竞态提供容器兜底：静态引用为空时经{@link SpringUtil}
 * 从容器解析并原子缓存；容器中亦不存在Bean时按断言抛出带装配指引的异常。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
public class UuidUtil {

    /**
     * 单例实例，volatile保证跨线程可见性与有序性
     */
    private static volatile UuidUtil instance;

    /**
     * UUID生成器实现
     */
    @Resource
    private IUuidGenerator uuidGenerator;

    /**
     * UUID配置属性
     */
    @Resource
    private UuidProperty uuidProperty;

    public UuidUtil() {
        instance = this;
    }

    /**
     * 获取UUID门面单例
     * <p>
     * 快路径直接返回静态引用；引用为空（Bean尚未实例化）时加锁经容器兜底解析并缓存；
     * 容器中亦不存在Bean时抛出带装配指引的断言异常。
     * </p>
     *
     * @return UUID门面单例
     */
    public static UuidUtil getInstance() {
        var inst = instance;
        if (inst == null) {
            synchronized (UuidUtil.class) {
                inst = instance;
                if (inst == null) {
                    inst = SpringUtil.getBeanOptional(UuidUtil.class).orElse(null);
                    if (inst == null) {
                        throw AssertUtil.isTrue(true).i18n("common.uuid.not.initialized")
                            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).build();
                    }
                    instance = inst;
                }
            }
        }
        return inst;
    }

    /**
     * 获取UUID字符串
     *
     * @return 按配置长度生成的UUID字符串
     */
    public String getUuidString() {
        return uuidGenerator.getUuidString(uuidProperty.getExceptLength());
    }

    /**
     * 获取UUID长整型
     *
     * @return 生成的UUID长整型
     */
    public Long getUuidLong() {
        return uuidGenerator.getUuidLong();
    }
}
