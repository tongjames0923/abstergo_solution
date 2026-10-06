package com.tbs.common.provider;

import com.tbs.common.provider.annotation.RoutingService;

/**
 * 路由演示服务接口，标注 @RoutingService 验证扫描注册（测试用）
 *
 * @author abstergo
 * @since 1.0.0
 */
@RoutingService(strategy = RoutingStrategy.LOCAL_FIRST)
interface RoutedService {

    /**
     * 心跳接口
     *
     * @return 固定响应
     */
    String ping();
}
