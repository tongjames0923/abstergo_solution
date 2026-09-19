package com.tbs.push.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.constants.PushI18nConstant;

import java.io.Serializable;

/**
 * 推送主题值对象
 * <p>
 * 业务消息的订阅主题（topic）的类型化表达，类型本身即承载格式约束：
 * <ul>
 *     <li>非空</li>
 *     <li>长度不超过 {@link PushFrameConstants#TOPIC_MAX_LENGTH}（128字符）</li>
 * </ul>
 * 经Jackson绑定时（HTTP请求体/协议帧解码）由{@code @JsonCreator}工厂自动执行校验，
 * 序列化时经{@code @JsonValue}还原为原始字符串，线上JSON报文格式不变。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class PushTopic implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主题原始字符串
     */
    private final String value;

    /**
     * 构造推送主题（私有，统一经 {@link #of(String)} 创建以保证约束生效）
     *
     * @param value 主题原始字符串
     */
    private PushTopic(String value) {
        this.value = value;
    }

    /**
     * 创建推送主题（Jackson反序列化入口）
     * <p>
     * 校验规则：非空且长度不超过{@link PushFrameConstants#TOPIC_MAX_LENGTH}，
     * 违反时抛出i18n断言异常。
     * </p>
     *
     * @param value 主题原始字符串
     * @return 推送主题值对象
     */
    @JsonCreator
    public static PushTopic of(String value) {
        AssertUtil.isTrue(StrUtil.isEmpty(value)).i18n(PushI18nConstant.TOPIC_REQUIRED).throwIt();
        AssertUtil.isTrue(value.length() > PushFrameConstants.TOPIC_MAX_LENGTH)
            .i18n(PushI18nConstant.TOPIC_TOO_LONG).params(PushFrameConstants.TOPIC_MAX_LENGTH).throwIt();
        return new PushTopic(value);
    }

    /**
     * 获取主题原始字符串
     *
     * @return 主题原始字符串（序列化时作为JSON字符串值输出）
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * 返回主题原始字符串
     *
     * @return 主题原始字符串
     */
    @Override
    public String toString() {
        return value;
    }

}
