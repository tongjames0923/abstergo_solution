package com.tbs.mq.config;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

/**
 * MQ抽象层自动组件注册类
 * <p>
 * 继承自AbstractAutoBeanRegister，扫描com.tbs.mq包下的默认装配组件
 * （如{@link MqSupportConfig}），实现MQ重试链路的配置化快速启动。
 * 重复导入时由基类按包名去重，多次扫描不会重复注册。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class MqPackAutoRegister extends AbstractAutoBeanRegister {

    /**
     * MQ抽象层功能模块
     */
    private static final Feature MQ_FEATURE = Feature.create("com.tbs.mq", "MQ消息抽象");

    /**
     * 获取包名称
     *
     * @return MQ抽象层包名称
     */
    @Override
    protected String getPackName() {
        return "MQ API Pack";
    }

    /**
     * 获取当前注册器对应的功能模块
     *
     * @return MQ抽象层功能模块
     */
    @Override
    protected Feature getFeature() {
        return MQ_FEATURE;
    }

    /**
     * 获取基础扫描包路径
     *
     * @return 基础扫描包路径数组
     */
    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.mq"};
    }

}
