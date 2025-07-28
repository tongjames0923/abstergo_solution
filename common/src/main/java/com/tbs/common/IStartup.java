package com.tbs.common;

import org.springframework.core.Ordered;

/**
 * 当Application 启动后运行
 *
 * @author abstergo
 */
public interface IStartup extends Ordered {
    /**
     * 运行方法
     *
     * @throws RuntimeException 可能得运行错误
     */
    void startUp() throws RuntimeException;

    /**
     * 启动运行顺序
     *
     * @return 顺序
     */
    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
