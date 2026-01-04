package com.tbs.rabbitmq.config;

import com.tbs.common.AbstractAutoBeanRegister;

/**
 * RabbitMQ自动组件注册类
 * <p>
 * 继承自AbstractAutoBeanRegister，用于自动扫描和注册RabbitMQ相关组件
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class RabbitMqAutoRegister extends AbstractAutoBeanRegister {
    /**
     * 获取包名称
     * <p>
     * 返回RabbitMQ组件包的名称，用于日志和标识
     * </p>
     *
     * @return RabbitMQ包名称
     */
    @Override
    protected String getPackName() {
        return "RabbitMQ Pack";
    }

    /**
     * 获取基础扫描包路径
     * <p>
     * 返回需要扫描的RabbitMQ相关组件的包路径数组
     * </p>
     *
     * @return 基础扫描包路径数组
     */
    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.rabbitmq"};
    }
}
