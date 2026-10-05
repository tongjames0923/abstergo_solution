package com.tbs.app.service.impl.push;

import com.tbs.common.util.base.StrUtil;
import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.api.session.ISubscriptionAuthorizer;
import com.tbs.push.constants.PushSessionAttrConstants;
import com.tbs.push.util.TopicMatcher;

/**
 * 授权集订阅校验器（订阅时点鉴权落地）
 * <p>
 * 判定顺序（短路）：
 * <ol>
 *     <li>本人信令频道精确放行（服务端绑定 userId 推导，与授权器产出对齐）；</li>
 *     <li>保留前缀 {@code tbs.push.channel.*} 一律拒绝——含通配窃听形态，
 *     即便授权集误配模式也不放行，杜绝跨用户信令元数据窃听；</li>
 *     <li>会话无授权集属性（未注册授权器/无鉴权部署）放行，保持历史行为；</li>
 *     <li>订阅主题命中授权集任一模式（精确相等或订阅侧通配匹配）放行，否则拒绝。</li>
 * </ol>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class GrantedTopicSubscriptionAuthorizer implements ISubscriptionAuthorizer {

    /**
     * 判定会话可否订阅指定主题
     *
     * @param session 发起订阅的会话（携带认证阶段绑定的身份与授权集属性）
     * @param topic   订阅主题原文
     * @return true表示允许订阅
     */
    @Override
    public boolean isAllowed(IConnectionSession session, String topic) {
        if (session == null || StrUtil.isEmpty(topic)) {
            return false;
        }
        String reservedPrefix = TopicPrefixNotifyReceiverResolver.RESERVED_SIGNAL_PREFIX;
        String userId = session.getAttribute(PushSessionAttrConstants.USER_ID);
        if (!StrUtil.isEmpty(userId) && topic.equals(reservedPrefix + userId)) {
            return true;
        }
        if (topic.startsWith(reservedPrefix)) {
            return false;
        }
        String granted = session.getAttribute(PushSessionAttrConstants.GRANTED_TOPICS);
        if (StrUtil.isEmpty(granted)) {
            return true;
        }
        for (String pattern : granted.split(PushSessionAttrConstants.GRANT_SEPARATOR)) {
            if (pattern.equals(topic) || TopicMatcher.matches(pattern, topic)) {
                return true;
            }
        }
        return false;
    }

}
