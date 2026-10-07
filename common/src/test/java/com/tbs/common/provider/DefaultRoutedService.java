package com.tbs.common.provider;

/**
 * 路由演示服务本地实现（测试用）
 *
 * @author abstergo
 * @since 1.0.0
 */
class DefaultRoutedService implements RoutedService {

    /**
     * 心跳接口
     *
     * @return 固定响应
     */
    @Override
    public String ping() {
        return "pong";
    }
}
