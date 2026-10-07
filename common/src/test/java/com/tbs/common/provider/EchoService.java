package com.tbs.common.provider;

/**
 * 演示服务接口（测试用）
 *
 * @author abstergo
 * @since 1.0.0
 */
interface EchoService {

    /**
     * 回显消息
     *
     * @param msg 消息内容
     * @return 回显结果
     */
    String echo(String msg);
}
