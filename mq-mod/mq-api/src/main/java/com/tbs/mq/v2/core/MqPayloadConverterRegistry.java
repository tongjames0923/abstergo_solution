package com.tbs.mq.v2.core;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息体转换器注册表（v2 记录驱动模型 · 按格式路由）
 * <p>
 * 汇集容器内全部 {@link IMqMessagePayloadConverter}，以各自的 {@code payloadFormat()} 为键建立路由表，
 * 供生产端序列化与消费端反序列化按消息头 {@code payloadFormat} 选取同一实现，保证两端格式严格对称。
 * 格式为空时回落到 {@link #DEFAULT_PAYLOAD_FORMAT}；目标格式无对应转换器时按系统错误断言拒绝（宁拒勿错解）。
 * </p>
 *
 * @author tongj
 * @since 2.0.0
 */
public class MqPayloadConverterRegistry {

    /**
     * 默认负载格式：入参未显式声明 payloadFormat 时采用的格式，与 JDK 序列化转换器一致
     */
    public static final String DEFAULT_PAYLOAD_FORMAT = "jdk";

    /**
     * 格式标识到转换器的路由表
     */
    private final Map<String, IMqMessagePayloadConverter> converterByFormat = new HashMap<>();

    /**
     * 构造转换器注册表
     *
     * @param converters 容器内的全部转换器实现，可为空
     */
    public MqPayloadConverterRegistry(List<IMqMessagePayloadConverter> converters) {
        if (converters != null) {
            for (IMqMessagePayloadConverter converter : converters) {
                if (converter != null && StrUtil.isNotEmpty(converter.payloadFormat())) {
                    converterByFormat.put(converter.payloadFormat(), converter);
                }
            }
        }
    }

    /**
     * 按负载格式解析转换器
     * <p>
     * 格式为空回落默认格式；无对应实现时抛系统错误，绝不静默选错转换器。
     * </p>
     *
     * @param payloadFormat 负载格式标识，可为空（空则取默认格式）
     * @return 匹配的转换器，不为空
     */
    public IMqMessagePayloadConverter resolve(String payloadFormat) {
        String format = StrUtil.isEmpty(payloadFormat) ? DEFAULT_PAYLOAD_FORMAT : payloadFormat;
        IMqMessagePayloadConverter converter = converterByFormat.get(format);
        AssertUtil.isTrueSystemError(converter == null).i18n("mq.v2.payload.converter.absent").params(format)
            .throwIt();
        return converter;
    }

    /**
     * 是否注册了指定格式
     *
     * @param payloadFormat 负载格式标识
     * @return true表示存在对应转换器
     */
    public boolean supports(String payloadFormat) {
        return converterByFormat.containsKey(payloadFormat);
    }
}
