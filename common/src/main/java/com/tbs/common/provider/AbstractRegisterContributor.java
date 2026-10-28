package com.tbs.common.provider;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.provider.vo.InstanceProxy;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
public abstract class AbstractRegisterContributor implements IRegisterContributor {

    /**
     * 根据类型获取实例
     *
     * @param type 类型
     * @return 实例
     */
    protected abstract <T> List<InstanceProxy> getInstanceByType(Class<T> type);

    /**
     * 获取所有实例
     *
     * @param type     类型
     * @param business 业务线
     * @param version  版本
     * @return 所有实例
     */
    @Override
    public List<InstanceProxy> getAllInstanceByTypeAndBusinessAndVersion(Class<?> type, String business,
        String version) {
        AssertUtil.isTrueSystemError(type == null).message("type不能为空").throwIt();
        var instanceProxies = getInstanceByType(type);
        if (CollUtil.isEmpty(instanceProxies)) {
            log.debug("没有找到类型为{}的实例", type.getName());
            return CollUtil.emptyList();
        }
        var stream = getStream(type, instanceProxies);
        if (StrUtil.isNotEmpty(business)) {
            stream = stream.filter(instanceProxy -> business.equals(instanceProxy.getBusiness()));
        }
        if (StrUtil.isNotEmpty(version)) {
            stream = stream.filter(instanceProxy -> version.equals(instanceProxy.getVersion()));
        }
        return stream.toList();
    }

    private static @NotNull Stream<InstanceProxy> getStream(Class<?> type, List<InstanceProxy> instanceProxies) {
        return instanceProxies.stream().peek(instanceProxy -> {
            instanceProxy.setInterfaceFullName(type.getName());
            String targetTypeName = instanceProxy.getInstance().getClass().getName();
            instanceProxy.setTargetTypeFullName(targetTypeName);
            if (instanceProxy.getInstance() instanceof IRouteMeta) {
                IRouteMeta routeMeta = (IRouteMeta)(instanceProxy.getInstance());
                if (StrUtil.isNotEmpty(routeMeta.business())) {
                    instanceProxy.setBusiness(routeMeta.business());
                }
                if (StrUtil.isNotEmpty(routeMeta.version())) {
                    instanceProxy.setVersion(routeMeta.version());
                }
                if (StrUtil.isNotEmpty(routeMeta.getTypeFullName())) {
                    instanceProxy.setTargetTypeFullName(routeMeta.getTypeFullName());
                }
            }
            log.debug("实例{}，业务线{}，版本{}", instanceProxy.getInstance(), instanceProxy.getBusiness(),
                instanceProxy.getVersion());
        }).toList().stream();
    }

}
