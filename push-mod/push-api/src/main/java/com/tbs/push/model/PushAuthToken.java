package com.tbs.push.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.push.constants.PushI18nConstant;

import java.io.Serializable;

/**
 * 业务认证令牌值对象
 * <p>
 * 连接认证表单中业务凭证（如登录令牌）的类型化表达，类型本身即承载格式约束：
 * <ul>
 *     <li>非空：认证必须携带凭证</li>
 *     <li>内容由业务认证器（IPushAuthenticator）解释，协议层不感知其格式语义</li>
 * </ul>
 * 经Jackson绑定时由{@code @JsonCreator}工厂自动执行校验。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class PushAuthToken implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 令牌原始字符串
     */
    private final String value;

    /**
     * 构造业务认证令牌（私有，统一经 {@link #of(String)} 创建以保证约束生效）
     *
     * @param value 令牌原始字符串
     */
    private PushAuthToken(String value) {
        this.value = value;
    }

    /**
     * 创建业务认证令牌（Jackson反序列化入口）
     * <p>
     * 校验规则：非空，违反时抛出i18n断言异常。
     * </p>
     *
     * @param value 令牌原始字符串
     * @return 业务认证令牌值对象
     */
    @JsonCreator
    public static PushAuthToken of(String value) {
        AssertUtil.isTrue(StrUtil.isEmpty(value)).i18n(PushI18nConstant.AUTH_TOKEN_REQUIRED).throwIt();
        return new PushAuthToken(value);
    }

    /**
     * 获取令牌原始字符串
     *
     * @return 令牌原始字符串（序列化时作为JSON字符串值输出）
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * 返回令牌原始字符串
     *
     * @return 令牌原始字符串
     */
    @Override
    public String toString() {
        return value;
    }

}
