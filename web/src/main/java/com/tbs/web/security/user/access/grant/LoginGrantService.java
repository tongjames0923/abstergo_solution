package com.tbs.web.security.user.access.grant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.web.constant.WebI18nConstant;
import com.tbs.web.security.token.ITokenModel;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 登录授权统一门面
 * <p>
 * 统一编排「按授权类型路由 → 启用校验 → 请求体反序列化 → 委托提供者签发令牌」，
 * 供统一登录端点（{@code POST /public/user/login/{grantType}}）调用。
 * 新增登录方式只需注册新 {@link ILoginGrantProvider}，本门面零改动；
 * 令牌签发链路（校验表单→拉取用户→校验链→签发→写登录信息）由提供者侧模板复用。
 * </p>
 *
 * @author abstergo
 * @see LoginGrantRegistry 登录授权注册表
 */
public class LoginGrantService {

    private final LoginGrantRegistry registry;

    private final ObjectMapper objectMapper;

    /**
     * 构建统一门面
     *
     * @param registry     登录授权注册表
     * @param objectMapper JSON 映射器（复用 Spring 容器实例）
     */
    public LoginGrantService(LoginGrantRegistry registry, ObjectMapper objectMapper) {
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    /**
     * 按授权类型换取访问令牌
     * <p>
     * 类型安全收敛在提供者内部：请求体经 {@link ObjectMapper#convertValue} 转为提供者声明的强类型表单，
     * 再交由提供者执行既有令牌签发模板。
     * </p>
     *
     * @param grantType 授权类型标识
     * @param payload   原始请求体，可为空（由各表单校验断言兜底）
     * @return 签发的访问令牌模型
     */
    public ITokenModel requestToken(String grantType, Map<String, Object> payload) {
        ILoginGrantProvider<?> provider = registry.getByGrantType(grantType);
        AssertUtil.isTrue(provider == null).i18n(WebI18nConstant.LOGIN_GRANT_NOT_SUPPORTED).params(grantType)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(!provider.isEnabled()).i18n(WebI18nConstant.LOGIN_GRANT_DISABLED).params(grantType)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        return doRequestToken(provider, convertPayload(provider, payload));
    }

    /**
     * 列出当前启用的登录授权方式元数据
     * <p>
     * 供「启用登录方式下发」接口导出，支撑前端登录方式清单由静态配置向后端下发演进。
     * </p>
     *
     * @return 启用方式元数据列表，无启用方式时返回空列表
     */
    public List<LoginGrantDescriptor> listEnabledGrants() {
        return registry.listAll().stream()
            .filter(ILoginGrantProvider::isEnabled)
            .map(provider -> new LoginGrantDescriptor(provider.getGrantType(), provider.getLabelKey(),
                provider.getPublicOptions()))
            .collect(Collectors.toList());
    }

    /**
     * 将原始请求体反序列化为提供者声明的强类型表单
     *
     * @param provider 登录授权提供者
     * @param payload  原始请求体
     * @return 强类型表单实例
     */
    private Object convertPayload(ILoginGrantProvider<?> provider, Map<String, Object> payload) {
        try {
            return objectMapper.convertValue(payload == null ? Collections.emptyMap() : payload, provider.getFormType());
        } catch (IllegalArgumentException e) {
            AssertUtil.isTrue(true).i18n(WebI18nConstant.LOGIN_GRANT_PAYLOAD_INVALID).params(provider.getGrantType())
                .cause(e).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
            return null;
        }
    }

    /**
     * 委托提供者签发令牌
     * <p>
     * 注册表已保证提供者与其声明表单类型一致，原始类型转换仅用于跨越通配符边界，安全性由注册期约束兜底。
     * </p>
     *
     * @param provider 登录授权提供者
     * @param form     强类型表单实例
     * @return 签发的访问令牌模型
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private ITokenModel doRequestToken(ILoginGrantProvider provider, Object form) {
        return provider.requestToken(((Class<? extends Serializable>) provider.getFormType()).cast(form));
    }
}
