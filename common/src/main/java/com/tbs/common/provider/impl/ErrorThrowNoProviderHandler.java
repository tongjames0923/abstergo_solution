package com.tbs.common.provider.impl;

import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.provider.INoProviderHandler;

/**
 * 抛错无提供者处理器
 * <p>
 * 未命中任何来源时直接抛业务异常，而不是返回 {@code null}：适合「缺提供者必须立刻暴露」的调用点，
 * 避免空值继续向下游传播成 NPE 或静默丢数据。异常消息走 i18n，带接口与坐标，便于定位是哪一路来源缺失。
 * </p>
 * <p>
 * 用法：{@code @RoutingRef(noProvider = ErrorThrowNoProviderHandler.class)}。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class ErrorThrowNoProviderHandler implements INoProviderHandler {

    /**
     * 无提供者处理
     *
     * @param business        业务分区，空串表示不约束
     * @param ver             发布版本，空串表示不约束
     * @param targetInterface 目标接口
     */
    @Override
    public void handle(String business, String ver, Class<?> targetInterface) {
        AssertUtil.isTrueBusinessError(true)
            .i18n(CommonI18nConstant.ROUTING_NO_PROVIDER_FOR_COORDINATE, targetInterface.getName(), business, ver)
            .throwIt();
    }
}
