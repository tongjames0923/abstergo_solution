package com.tbs.common.provider;

import com.tbs.common.provider.vo.InstanceProxy;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * 注册贡献者
 *
 * @author Boolean
 */
public interface IRegisterContributor extends Ordered {

    /**
     * 获取注册贡献者ID
     *
     * @return 注册贡献者ID
     */
    String getContributorId();

    /**
     * 获取所有实例
     *
     * @param type     类型
     * @param business 业务线
     * @param version  版本
     * @return 所有实例
     */
    List<InstanceProxy> getAllInstanceByTypeAndBusinessAndVersion(Class<?> type, String business,
        String version);

    /**
     * Get the order value of this object.
     * <p>Higher values are interpreted as lower priority. As a consequence,
     * the object with the lowest value has the highest priority (somewhat analogous to Servlet {@code load-on-startup}
     * values).
     * <p>Same order values will result in arbitrary sort positions for the
     * affected objects.
     *
     * @return the order value
     * @see #HIGHEST_PRECEDENCE
     * @see #LOWEST_PRECEDENCE
     */
    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
