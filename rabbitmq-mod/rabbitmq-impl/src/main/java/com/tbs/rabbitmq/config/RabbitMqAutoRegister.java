package com.tbs.rabbitmq.config;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

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
     * 获取当前注册器对应的功能模块
     *
     * @return RabbitMQ功能模块
     */
    @Override
    protected Feature getFeature() {
        return Feature.RABBITMQ;
    }

    /**
     * 获取基础扫描包路径
     * <p>
     * 除RabbitMQ实现包外，同时扫描MQ抽象层包（com.tbs.mq），
     * 使@EnableRabbitMq一步完成厂商实现与抽象层默认装配的快速启动。
     * </p>
     *
     * @return 基础扫描包路径数组
     */
    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.rabbitmq", "com.tbs.mq"};
    }
}
