package com.tbs.common.provider.impl.coordinator;

import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.provider.IRouteCoordinator;
import com.tbs.common.util.base.StrUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 包前缀路由协调器
 * <p>
 * 只接受实现类全限定名以指定包前缀开头的候选，用于把寻址限定在本应用或某个模块内：
 * 例如只要本地实现时传本应用包前缀，隔离灰度实现时传灰度包前缀。
 * </p>
 * <p>
 * 不注册为 Bean：同一容器需要多种前缀策略，由调用方按需构造后传给注册中心。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class PackagePrefixRouteCoordinator implements IRouteCoordinator {

    /**
     * 包前缀白名单，命中任一前缀即接受
     */
    private final List<String> packagePrefixes;

    /**
     * 构造包前缀协调器
     *
     * @param packagePrefixes 包前缀白名单，不能为空
     */
    public PackagePrefixRouteCoordinator(String... packagePrefixes) {
        AssertUtil.isTrueBusinessError(packagePrefixes == null || packagePrefixes.length == 0)
            .i18n(CommonI18nConstant.INVALID_PARAMETER).throwIt();
        this.packagePrefixes = Arrays.stream(packagePrefixes).filter(StrUtil::isNotBlank).toList();
    }

    /**
     * 裁决候选实例
     *
     * @param targetClass    候选实例类型
     * @param interfaceClass 查询接口
     * @param business       业务分区，空串表示不约束
     * @param version        发布版本，空串表示不约束
     * @return 实现在白名单前缀下为 true
     */
    @Override
    public boolean route(Class<?> targetClass, Class<?> interfaceClass, String business, String version) {
        String className = targetClass.getName();
        return packagePrefixes.stream().anyMatch(className::startsWith);
    }
}
