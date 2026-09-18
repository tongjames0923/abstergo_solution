package com.tbs.push.websocket.config;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

/**
 * 推送WebSocket传输适配自动组件注册类
 * <p>
 * 继承自AbstractAutoBeanRegister，扫描com.tbs.push.websocket包下的装配组件，
 * 重复导入时由基类按包名去重。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class WebSocketPushAutoRegister extends AbstractAutoBeanRegister {

    /**
     * WebSocket传输适配功能模块
     */
    private static final Feature WEBSOCKET_PUSH_FEATURE = Feature.create("com.tbs.push.websocket", "推送WebSocket传输");

    /**
     * 获取包名称
     *
     * @return 适配层包名称
     */
    @Override
    protected String getPackName() {
        return "Push WebSocket Pack";
    }

    /**
     * 获取当前注册器对应的功能模块
     *
     * @return WebSocket传输适配功能模块
     */
    @Override
    protected Feature getFeature() {
        return WEBSOCKET_PUSH_FEATURE;
    }

    /**
     * 获取基础扫描包路径
     *
     * @return 基础扫描包路径数组
     */
    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.push.websocket"};
    }

}
