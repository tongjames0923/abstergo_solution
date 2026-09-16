package com.tbs.web.security.user.access.grant;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.web.constant.WebI18nConstant;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录授权类型注册表
 * <p>
 * Spring 容器自动收集全部 {@link ILoginGrantProvider} 实现并按授权类型建立映射，
 * 供统一登录门面按 {@code grantType} O(1) 路由；重复注册在启动期即失败（fail-fast）。
 * </p>
 *
 * @author abstergo
 * @see ILoginGrantProvider 登录授权提供者 SPI
 */
public class LoginGrantRegistry {

    private final Map<String, ILoginGrantProvider<?>> providerMap;

    /**
     * 构建注册表
     *
     * @param providers 容器中全部登录授权提供者，可为空（此时注册表为空表）
     */
    public LoginGrantRegistry(List<ILoginGrantProvider<?>> providers) {
        this.providerMap = new HashMap<>();
        if (CollUtil.isEmpty(providers)) {
            return;
        }
        for (ILoginGrantProvider<?> provider : providers) {
            ILoginGrantProvider<?> previous = providerMap.put(provider.getGrantType(), provider);
            AssertUtil.isTrue(previous != null).i18n(WebI18nConstant.LOGIN_GRANT_TYPE_DUPLICATE)
                .params(provider.getGrantType()).errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        }
    }

    /**
     * 按授权类型查找提供者
     *
     * @param grantType 授权类型标识
     * @return 对应提供者；未注册时返回 {@code null}
     */
    public ILoginGrantProvider<?> getByGrantType(String grantType) {
        return providerMap.get(grantType);
    }

    /**
     * 列出全部已注册提供者
     *
     * @return 提供者集合，无注册时返回空集合
     */
    public Collection<ILoginGrantProvider<?>> listAll() {
        return providerMap.values();
    }
}
