package com.tbs.common.provider;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.provider.vo.InstanceMeta;
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
                InstanceMeta meta;
                try {
                    meta = routeMeta.getMeta();
                } catch (Exception e) {
                    // 元信息拉取失败（远端实例版本落后于接口、调用异常等）时按空业务线与空版本退化导入，
                    // 该实例不再受坐标约束仍参与选择，避免单个实例的元信息故障中断整条来源链
                    log.warn("实例{}元信息获取失败，按空Business和空Version退化导入，原因: {}", instanceProxy.getInstance(),
                        e.getMessage());
                    instanceProxy.setBusiness(StrUtil.EMPTY);
                    instanceProxy.setVersion(StrUtil.EMPTY);
                    meta = null;
                }
                if (meta == null) {
                    meta = new InstanceMeta();
                }
                if (StrUtil.isNotEmpty(meta.getBusiness())) {
                    instanceProxy.setBusiness(meta.getBusiness());
                }
                if (StrUtil.isNotEmpty(meta.getVersion())) {
                    instanceProxy.setVersion(meta.getVersion());
                }
                if (StrUtil.isNotEmpty(meta.getClassFullName())) {
                    instanceProxy.setTargetTypeFullName(meta.getClassFullName());
                }
            }
            log.debug("实例{}，业务线{}，版本{}", instanceProxy.getInstance(), instanceProxy.getBusiness(),
                instanceProxy.getVersion());
        }).toList().stream();
    }

}
