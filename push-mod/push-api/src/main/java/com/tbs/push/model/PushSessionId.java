package com.tbs.push.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.push.constants.PushI18nConstant;

import java.io.Serializable;

/**
 * 推送会话标识值对象
 * <p>
 * 连接会话唯一标识（sessionId）的类型化表达，类型本身即承载格式约束：
 * <ul>
 *     <li>非空</li>
 *     <li>由服务端在认证阶段预分配（UUID形态），经握手确认帧下发，
 *     客户端上行时原样回传以定位会话</li>
 * </ul>
 * 经Jackson绑定时由{@code @JsonCreator}工厂自动执行校验；
 * 是否真实存在由协议层会话注册表在处理时校验，本类型只承载格式边界。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class PushSessionId implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话标识原始字符串
     */
    private final String value;

    /**
     * 构造会话标识（私有，统一经 {@link #of(String)} 创建以保证约束生效）
     *
     * @param value 会话标识原始字符串
     */
    private PushSessionId(String value) {
        this.value = value;
    }

    /**
     * 创建会话标识（Jackson反序列化入口）
     * <p>
     * 校验规则：非空，违反时抛出i18n断言异常。
     * </p>
     *
     * @param value 会话标识原始字符串
     * @return 会话标识值对象
     */
    @JsonCreator
    public static PushSessionId of(String value) {
        AssertUtil.isTrue(StrUtil.isEmpty(value)).i18n(PushI18nConstant.SESSION_ID_REQUIRED).throwIt();
        return new PushSessionId(value);
    }

    /**
     * 获取会话标识原始字符串
     *
     * @return 会话标识原始字符串（序列化时作为JSON字符串值输出）
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * 返回会话标识原始字符串
     *
     * @return 会话标识原始字符串
     */
    @Override
    public String toString() {
        return value;
    }

}
