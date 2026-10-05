package com.tbs.push.api.session;

/**
 * 订阅授权校验器 SPI
 * <p>
 * SUBSCRIBE帧进入订阅注册表前的鉴权决策点：协议层仅持有会话与主题两个
 * 传输无关的事实，"什么身份能订什么主题"的语义全部归属业务实现Bean。
 * 未注册实现Bean时订阅行为与历史一致（全放行），保证无鉴权部署兼容；
 * 业务实现建议以会话属性中的授权集（{@code grantedTopics}）为判定依据。
 * 校验不通过时协议层回发ERROR帧拒绝订阅，不关闭连接。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ISubscriptionAuthorizer {

    /**
     * 判定会话可否订阅指定主题
     *
     * @param session 发起订阅的会话（携带认证阶段绑定的身份与授权集属性）
     * @param topic   订阅主题原文
     * @return true表示允许订阅
     */
    boolean isAllowed(IConnectionSession session, String topic);

}
