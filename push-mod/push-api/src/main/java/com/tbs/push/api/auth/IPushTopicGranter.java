package com.tbs.push.api.auth;

import com.tbs.push.model.auth.PushAuthResult;

import java.util.List;

/**
 * 主题授权器 SPI
 * <p>
 * 凭证签发时点的授权集计算扩展点：认证通过后由协议层链式调用全部实现Bean，
 * 合并产出该会话可订阅的主题模式列表，写入会话属性{@code grantedTopics}
 * 并随凭证下发给客户端（供按需订阅展示），订阅校验点据此判定放行与否。
 * 分层约定：硬编码层（本人专属频道）与配置白名单层由业务内置实现，
 * 其余授权来源（角色/权限组/外部授权系统）由业务方注册额外实现Bean介入，
 * 单个授权器异常仅跳过不影响其余产出。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IPushTopicGranter {

    /**
     * 计算当前认证结果对应的可订阅主题模式列表
     *
     * @param authResult 认证通过结果（含服务端绑定的身份属性）
     * @return 主题模式列表（支持订阅侧通配符），允许为null或空
     */
    List<String> grantTopics(PushAuthResult authResult);

}
