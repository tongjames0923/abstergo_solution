package com.tbs.app.service.impl.push;

import com.tbs.common.util.base.StrUtil;
import com.tbs.push.api.auth.IPushTopicGranter;
import com.tbs.push.config.property.PushProperty;
import com.tbs.push.constants.PushSessionAttrConstants;
import com.tbs.push.model.auth.PushAuthResult;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 内置主题授权器（授权集的硬编码层与配置层）
 * <p>
 * 两层来源合并产出：
 * <ul>
 *     <li>硬编码层：本人信令频道 {@code tbs.push.channel.{userId}}，
 *     userId 取认证器服务端绑定值，客户端声明不参与；</li>
 *     <li>配置层：{@code tbs.push.grant-public-topics} 公开主题白名单，
 *     无条件授予全部认证会话。</li>
 * </ul>
 * 其余授权来源（角色/权限组/外部授权系统）由业务方注册额外
 * {@link IPushTopicGranter} 实现Bean介入，协议层链式合并。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SignalChannelTopicGranter implements IPushTopicGranter {

    /**
     * 推送基础配置属性（公开主题白名单来源）
     */
    @Resource
    private PushProperty pushProperty;

    /**
     * 计算授权集：本人信令频道 + 配置公开白名单
     *
     * @param authResult 认证通过结果（含服务端绑定的 userId 属性）
     * @return 主题模式列表
     */
    @Override
    public List<String> grantTopics(PushAuthResult authResult) {
        List<String> granted = new ArrayList<>();
        Map<String, String> attributes = authResult == null ? null : authResult.getAttributes();
        String userId = attributes == null ? null : attributes.get(PushSessionAttrConstants.USER_ID);
        if (!StrUtil.isEmpty(userId)) {
            granted.add(TopicPrefixNotifyReceiverResolver.RESERVED_SIGNAL_PREFIX + userId);
        }
        if (pushProperty != null && pushProperty.getGrantPublicTopics() != null) {
            for (String topic : pushProperty.getGrantPublicTopics()) {
                if (!StrUtil.isEmpty(topic)) {
                    granted.add(topic.trim());
                }
            }
        }
        return granted;
    }

}
