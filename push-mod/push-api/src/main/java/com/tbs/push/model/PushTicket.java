package com.tbs.push.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.push.constants.PushI18nConstant;

import java.io.Serializable;

/**
 * 连接凭证值对象
 * <p>
 * 认证端点签发的一次性建连凭证（push-ticket）的类型化表达，类型本身即承载格式约束：
 * <ul>
 *     <li>非空</li>
 *     <li>由认证端点签发、一次性有效（消费即失效）、带TTL过期时间</li>
 *     <li>原文不落日志（协议层日志仅保留前缀脱敏）</li>
 * </ul>
 * 经Jackson绑定时由{@code @JsonCreator}工厂自动执行校验。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class PushTicket implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 凭证原始字符串
     */
    private final String value;

    /**
     * 构造连接凭证（私有，统一经 {@link #of(String)} 创建以保证约束生效）
     *
     * @param value 凭证原始字符串
     */
    private PushTicket(String value) {
        this.value = value;
    }

    /**
     * 创建连接凭证（Jackson反序列化入口）
     * <p>
     * 校验规则：非空，违反时抛出i18n断言异常。有效性（存在性/一次性/过期）由
     * 协议层凭证服务在建连时校验，本类型只承载格式边界。
     * </p>
     *
     * @param value 凭证原始字符串
     * @return 连接凭证值对象
     */
    @JsonCreator
    public static PushTicket of(String value) {
        AssertUtil.isTrue(StrUtil.isEmpty(value)).i18n(PushI18nConstant.TICKET_REQUIRED).throwIt();
        return new PushTicket(value);
    }

    /**
     * 获取凭证原始字符串
     *
     * @return 凭证原始字符串（序列化时作为JSON字符串值输出）
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * 返回凭证原始字符串
     *
     * @return 凭证原始字符串
     */
    @Override
    public String toString() {
        return value;
    }

}
