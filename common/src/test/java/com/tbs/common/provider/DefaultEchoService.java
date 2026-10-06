package com.tbs.common.provider;

import lombok.extern.slf4j.Slf4j;

/**
 * 演示服务本地实现（测试用）
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
class DefaultEchoService implements EchoService {

    /**
     * 回显消息
     *
     * @param msg 消息内容
     * @return 回显结果
     */
    @Override
    public String echo(String msg) {
        return "echo:" + msg;
    }
}
