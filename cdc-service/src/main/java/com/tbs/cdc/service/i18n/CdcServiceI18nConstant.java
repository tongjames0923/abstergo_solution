package com.tbs.cdc.service.i18n;

/**
 * CDC服务模块国际化资源key常量
 * <p>
 * 集中声明断言异常文案的i18n key，与{@code messages-cdc-service.properties}资源文件一一对应。
 * 所有面向日志与异常的文案经此引用，禁止硬编码魔法值。参考CdcI18nConstant模式。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class CdcServiceI18nConstant {

    /**
     * MQ发送配置exchange缺失
     */
    public static final String MQ_EXCHANGE_REQUIRED = "cdc.mq.exchange.required";

    /**
     * MQ发送配置routingPattern缺失
     */
    public static final String MQ_ROUTING_PATTERN_REQUIRED = "cdc.mq.routing.pattern.required";

    /**
     * 工具类禁止实例化
     */
    private CdcServiceI18nConstant() {
    }
}
