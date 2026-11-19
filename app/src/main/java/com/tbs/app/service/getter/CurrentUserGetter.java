package com.tbs.app.service.getter;

import com.tbs.script.api.expressible.vo.*;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.uitl.AuthUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 当前用户信息取值器。
 *
 * <p>读取当前登录用户的会话信息，作为用户维度的事件条件来源。仅暴露非敏感字段，
 * 不会返回令牌原文；未登录、参数缺失或取值异常时返回空字符串，不中断脚本执行。</p>
 *
 * @author abstergo
 */
@Component
public class CurrentUserGetter extends AbstractStringValueGetter {

    /**
     * 信息类型枚举值：是否已登录
     */
    private static final String TYPE_LOGGED_IN = "loggedIn";

    /**
     * 信息类型枚举值：用户名
     */
    private static final String TYPE_USER_NAME = "userName";

    /**
     * 信息类型枚举值：会话 ID
     */
    private static final String TYPE_SESSION_ID = "sessionId";

    /**
     * 信息类型枚举值：令牌过期时间
     */
    private static final String TYPE_EXPIRE_TIME = "expireTime";

    /**
     * 信息类型枚举值：令牌类型
     */
    private static final String TYPE_TOKEN_TYPE = "tokenType";

    /**
     * 信息类型枚举值：令牌状态
     */
    private static final String TYPE_STATUS = "status";

    /**
     * 信息类型文案 key 前缀
     */
    private static final String I18N_PREFIX = "value.getter.current-user.type.";

    /**
     * 认证工具类
     */
    @Resource
    private AuthUtil authUtil;

    /**
     * 获取取值器名称。
     *
     * @return 国际化后的取值器名称
     */
    @Override
    public String getGetterName() {
        return text("value.getter.current-user.name");
    }

    /**
     * 获取取值器 ID。
     *
     * @return 取值器 ID
     */
    @Override
    public String getGetterId() {
        return "CurrentUserGetter";
    }

    /**
     * 获取取值器描述。
     *
     * @return 国际化后的取值器描述
     */
    @Override
    public String getDescription() {
        return text("value.getter.current-user.description");
    }

    /**
     * 声明参数定义：一个必填的信息类型枚举参数。
     *
     * @return 参数定义列表
     */
    @Override
    public List<ParamType> requestParamTypes() {
        List<EnumValue> enumValues = List.of(enumValue(TYPE_LOGGED_IN, "logged-in"),
            enumValue(TYPE_USER_NAME, "user-name"), enumValue(TYPE_SESSION_ID, "session-id"),
            enumValue(TYPE_EXPIRE_TIME, "expire-time"), enumValue(TYPE_TOKEN_TYPE, "token-type"),
            enumValue(TYPE_STATUS, "status"));
        return List.of(ParamType.builder().isEnum(true).type("string")
            .name(text("value.getter.current-user.param.type.name"))
            .description(text("value.getter.current-user.param.type.description")).enumValues(enumValues)
            .required(true).sort(1)
            .ui(ParamUiMeta.builder().inputType("select")
                .placeholder(text("value.getter.current-user.param.type.placeholder")).build())
            .build());
    }

    /**
     * 按信息类型读取当前用户会话信息。
     *
     * @param paramValues 参数值集合，首个参数为信息类型
     * @return 字符串结果；未登录、类型未知或异常时返回空字符串
     */
    @Override
    public ResultValue getValue(Collection<ParamValue> paramValues) {
        String type = paramString(paramValues, 0);
        if (type == null || type.isBlank()) {
            return emptyResult();
        }
        try {
            return stringResult(switch (type) {
                case TYPE_LOGGED_IN -> String.valueOf(authUtil.isUserLoggedIn());
                case TYPE_USER_NAME -> currentToken().map(ITokenModel::getUserName).orElse("");
                case TYPE_SESSION_ID -> currentToken().map(ITokenModel::getSessionId).orElse("");
                case TYPE_EXPIRE_TIME -> currentToken().map(ITokenModel::getExpireTime).map(date -> String.valueOf(
                    date.getTime())).orElse("");
                case TYPE_TOKEN_TYPE -> currentToken().map(ITokenModel::getTokenType).map(Enum::name).orElse("");
                case TYPE_STATUS -> currentToken().map(ITokenModel::getStatus).map(Enum::name).orElse("");
                default -> "";
            });
        } catch (Exception e) {
            return emptyResult();
        }
    }

    /**
     * 安全读取当前令牌模型。
     *
     * @return 令牌模型；未登录或读取失败时为空
     */
    private Optional<ITokenModel> currentToken() {
        try {
            return Optional.ofNullable(authUtil.getCurrentTokenModel());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 构造信息类型枚举项。
     *
     * @param value    枚举值
     * @param labelKey 文案 key 后缀
     * @return 枚举项
     */
    private EnumValue enumValue(String value, String labelKey) {
        return EnumValue.builder().value(value).label(text(I18N_PREFIX + labelKey)).build();
    }
}
