package com.tbs.common.provider.impl;

import com.tbs.common.constant.ProviderConstant;
import com.tbs.common.provider.IRegisterContributor;
import com.tbs.common.provider.IRegistryCenter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 缺省注册中心
 * <p>
 * 贡献者由容器注入：容器内的全部 {@link IRegisterContributor} 按 {@link Ordered} 排序后装配为初始库存， 该顺序即来源优先级，与
 * {@link IRegistryCenter#getInstanceProxy} 的「先命中先返回」一致。 注入取 {@link ObjectProvider}，贡献者被开关全部关掉时退化为空库存而非启动失败。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(prefix = ProviderConstant.PROPERTY_PREFIX, name = ProviderConstant.ENABLE_PROPERTY_NAME,
    matchIfMissing = true)
public class DefaultRegistryCenter implements IRegistryCenter {

    /**
     * 贡献者库存，按优先级排列，允许运行期增删
     */
    private final List<IRegisterContributor> contributors;

    /**
     * 构造注册中心
     *
     * @param contributorProvider 容器内的贡献者提供器，由容器注入
     */
    public DefaultRegistryCenter(ObjectProvider<IRegisterContributor> contributorProvider) {
        this.contributors = new CopyOnWriteArrayList<>(contributorProvider.orderedStream().toList());
    }

    /**
     * 获取所有注册贡献者
     *
     * @return 按优先级排列的贡献者快照
     */
    @Override
    public List<IRegisterContributor> getAllContributors() {
        return List.copyOf(contributors);
    }

    /**
     * 注册贡献者
     * <p>
     * 追加后按优先级重排，运行期新增的来源不会被排到链尾而丢失优先级。
     * </p>
     *
     * @param contributor 贡献者
     */
    @Override
    public void register(IRegisterContributor contributor) {
        contributors.add(contributor);
        contributors.sort(Comparator.comparingInt(IRegisterContributor::getOrder));
    }

    /**
     * 注销贡献者
     *
     * @param contributor 贡献者
     */
    @Override
    public void unregister(IRegisterContributor contributor) {
        contributors.remove(contributor);
    }
}
