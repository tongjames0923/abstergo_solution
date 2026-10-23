package com.tbs.common.provider;

/**
 * 懒条目贡献者契约。
 * <p>
 * 注册中心的按需填充通道：中心某接口桶在读取时若尚无该贡献者的条目，回调
 * {@link #contributeEntries(IRegistryCenter, Class)} 让来源现场解析并推送条目入中心，
 * 使「来源何时扫容器/查注册中心」由首次读取驱动，装配期不做全量预扫描。
 * </p>
 * <p>
 * 贡献的新鲜度策略由实现自行声明：{@link #refreshOnRead()} 为 true 时中心每次读取都回调贡献
 * （动态来源如 Dubbo，节流责任在来源自身），为 false 时中心按接口一次性触发首贡献（静态来源如 Spring，
 * 每个接口首次被读取时完成贡献，之后不再重扫）。贡献实现应幂等：重复贡献同一 key 只反映最新快照。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 * @see IRegistrationSource
 */
public interface ILazyEntriesContributor {

    /**
     * 本贡献者推送条目所归属的来源
     *
     * @return 来源标识，不能为null
     */
    ObjectSource source();

    /**
     * 是否要求中心在每次读取时都重新贡献（动态来源返回 true）
     *
     * @return true 表示读即重贡献，false 表示每接口只首贡献一次
     */
    boolean refreshOnRead();

    /**
     * 现场解析指定接口的全部实例并推送条目入中心
     * <p>
     * 实现应经 {@link IRegistryCenter#replaceEntriesBySource} 整段替换本源在该接口下的条目；
     * 来源不可用时贡献空集并记录日志，不得抛出异常打断读取。
     * </p>
     *
     * @param center 注册中心，不能为null
     * @param key    目标接口类型，不能为null
     */
    void contributeEntries(IRegistryCenter center, Class<?> key);
}
