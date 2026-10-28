package com.tbs.common.provider;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.provider.vo.InstanceProxy;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class AbstractRegisterContributor implements IRegisterContributor {

    /**
     * 根据类型获取实例
     *
     * @param type 类型
     * @return 实例
     */
    protected abstract <T> List<InstanceProxy<T>> getInstanceByType(Class<T> type);

    /**
     * 获取所有实例
     *
     * @param type     类型
     * @param business 业务线
     * @param version  版本
     * @return 所有实例
     */
    @Override
    public <T> List<InstanceProxy<T>> getAllInstanceByTypeAndBusinessAndVersion(Class<T> type, String business,
        String version) {
        AssertUtil.isTrueSystemError(type == null).message("type不能为空").throwIt();
        var instanceProxies = getInstanceByType(type);
        if (CollUtil.isEmpty(instanceProxies)) {
            log.debug("没有找到类型为{}的实例", type.getName());
            return CollUtil.emptyList();
        }
        var stream = instanceProxies.stream();
        if (StrUtil.isNotEmpty(business)) {
            stream = stream.filter(instanceProxy -> business.equals(instanceProxy.getBusiness()));
        }
        if (StrUtil.isNotEmpty(version)) {
            stream = stream.filter(instanceProxy -> version.equals(instanceProxy.getVersion()));
        }
        return stream.toList();
    }

}
