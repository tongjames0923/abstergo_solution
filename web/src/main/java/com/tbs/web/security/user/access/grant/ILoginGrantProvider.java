package com.tbs.web.security.user.access.grant;

import com.tbs.web.security.user.access.IUserAccessTokenProvider;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * 登录授权类型提供者 SPI
 * <p>
 * 在 {@link IUserAccessTokenProvider} 之上增加自描述能力（授权类型标识、凭证表单类型、启用开关），
 * 由 {@link LoginGrantRegistry} 自动收集，统一登录端点按 {@code grantType} 分发。
 * 新增登录方式只需实现本接口并注册为 Spring Bean，控制器与门面零改动。
 * 授权类型标识约定与前端登录方式 Provider id 对齐（如 {@code password}、{@code wecom}）。
 * </p>
 *
 * @param <FORM> 凭证表单类型，需可序列化
 * @author abstergo
 * @see IUserAccessTokenProvider 访问令牌提供者基类（实现方可继承其抽象模板复用令牌签发链路）
 */
public interface ILoginGrantProvider<FORM extends Serializable> extends IUserAccessTokenProvider<FORM> {

    /**
     * 获取授权类型唯一标识
     * <p>
     * 全局唯一，重复注册在启动期即失败；与前端登录方式 Provider id 对齐。
     * </p>
     *
     * @return 授权类型标识（如 password / wecom）
     */
    String getGrantType();

    /**
     * 获取凭证表单类型
     * <p>
     * 供统一门面将原始请求体反序列化为强类型表单。
     * </p>
     *
     * @return 凭证表单 Class
     */
    Class<FORM> getFormType();

    /**
     * 当前授权方式是否启用
     * <p>
     * 建议绑定各自配置总开关（{@code tbs.{module}.enable}）；未启用方式在统一端点与元数据导出中均被拒绝。
     * </p>
     *
     * @return 启用返回 {@code true}
     */
    boolean isEnabled();

    /**
     * 获取展示文案 i18n key
     * <p>
     * 默认按前端 i18n 命名约定 {@code login.method.{grantType}} 生成，特殊命名时覆写。
     * </p>
     *
     * @return 展示文案 i18n key
     */
    default String getLabelKey() {
        return "login.method." + getGrantType();
    }

    /**
     * 获取可下发给前端的公开参数
     * <p>
     * 仅允许放置公开参数（如企微 corpId/agentId）；secret 等敏感配置禁止导出。
     * </p>
     *
     * @return 公开参数映射，无参数时返回空映射
     */
    default Map<String, Object> getPublicOptions() {
        return Collections.emptyMap();
    }
}
