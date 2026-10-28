package com.tbs.common.provider;

import com.tbs.common.provider.vo.InstanceProxy;
import com.tbs.common.util.base.CollUtil;

import java.util.List;

public interface IRegistryCenter {

    /**
     * 获取所有注册贡献者
     *
     * @return 所有注册贡献者
     */
    List<IRegisterContributor> getAllContributors();

    /**
     * 注册贡献者
     *
     * @param contributor 贡献者
     */
    void register(IRegisterContributor contributor);

    /**
     * 注销贡献者
     *
     * @param contributor 贡献者
     */
    void unregister(IRegisterContributor contributor);



    /**
     * 获取实例代理
     *
     * @param type     类型
     * @param business 业务线
     * @param version  版本
     * @param <T>      实例类型
     * @return 实例代理
     */
    default <T> InstanceProxy<T> getInstanceProxy(Class<T> type, String business, String version) {
        var contributors = getAllContributors();
        for (var contributor : contributors) {
            var proxys = contributor.getAllInstanceByTypeAndBusinessAndVersion(type, business, version);
            if (CollUtil.isNotEmpty(proxys)) {
                InstanceProxy<T> proxy = new InstanceProxy<>();
                proxy.setContributor(contributor);
                proxy.setInstance(proxys.get(0));
                proxy.setVersion(version);
                proxy.setBusiness(business);
                proxy.setRegistryCenter(this);
                return proxy;
            }
        }
        return null;
    }
}
