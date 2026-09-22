package com.tbs.app.service.impl.push;

import com.tbs.app.form.PushNotifySendForm;
import com.tbs.app.service.push.INotifyReceiverResolver;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;

import javax.annotation.Resource;
import java.util.List;

/**
 * 基于 topic 前缀的接收人解析器（新链路默认实现）
 * <p>
 * 按消息 topic 前缀判定投递方式，再查询用户表完成精准目标用户匹配：
 * <ul>
 *     <li>{@code global.broadcast}（或 topic 缺省）：全体激活用户广播，按用户表全量查询；</li>
 *     <li>{@code user.name.{userName}}：按用户名精确匹配单个/同名的多个用户；</li>
 *     <li>{@code user.search.{keyword}}：按用户名前缀搜索匹配多个用户；</li>
 *     <li>{@code tbs.push.channel.*}：信令保留频道，拒绝解析（防止业务消息伪装信令）；</li>
 *     <li>其他未知 topic：返回空集（无 fan-out、无信令，仅落库审计语义）。</li>
 * </ul>
 * 经 BuisnessBeanConfig 以 @ConditionalOnMissingBean 装配，业务方注册自定义实现 Bean 即覆盖本实现。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class TopicPrefixNotifyReceiverResolver implements INotifyReceiverResolver {

    /**
     * 全体广播 topic（与 PushNotifyServiceImpl.DEFAULT_TOPIC 缺省一致）
     */
    public static final String TOPIC_BROADCAST = "global.broadcast";

    /**
     * 用户精确匹配 topic 前缀：user.name.{userName}
     */
    public static final String TOPIC_USER_NAME_PREFIX = "user.name.";

    /**
     * 用户名前缀搜索 topic 前缀：user.search.{keyword}
     */
    public static final String TOPIC_USER_SEARCH_PREFIX = "user.search.";

    /**
     * 信令保留频道前缀（业务 topic 不得占用，与 PushNotifyServiceImpl.CHANNEL_TOPIC_PREFIX 一致）
     */
    public static final String RESERVED_SIGNAL_PREFIX = "tbs.push.channel.";

    /**
     * 系统用户服务（用户表查询与搜索数据源）
     */
    @Resource
    private ISystemUserService systemUserService;

    /**
     * 支持除信令保留频道外的全部 topic（含缺省 topic=全体广播语义）；未知 topic 命中后返回空集
     *
     * @param form 发送表单
     * @return 信令保留频道返回false，其余返回true
     */
    @Override
    public boolean supports(PushNotifySendForm form) {
        String topic = form == null ? null : form.getTopic();
        if (StrUtil.isEmpty(topic)) {
            return true;
        }
        return !topic.trim().startsWith(RESERVED_SIGNAL_PREFIX);
    }

    /**
     * 按 topic 前缀分派查询用户表，返回去重前的接收人列表（Service 侧兜底去重）
     *
     * @param form 发送表单
     * @return 接收用户ID列表，可为空列表
     */
    @Override
    public List<Long> resolveReceivers(PushNotifySendForm form) {
        String topic = form == null ? null : form.getTopic();
        topic = StrUtil.isEmpty(topic) ? TOPIC_BROADCAST : topic.trim();
        if (topic.startsWith(TOPIC_USER_NAME_PREFIX)) {
            String username = topic.substring(TOPIC_USER_NAME_PREFIX.length()).trim();
            if (StrUtil.isEmpty(username)) {
                return CollUtil.emptyList();
            }
            return systemUserService.queryUserIdsByUserName(username);
        }
        if (topic.startsWith(TOPIC_USER_SEARCH_PREFIX)) {
            String keyword = topic.substring(TOPIC_USER_SEARCH_PREFIX.length()).trim();
            if (StrUtil.isEmpty(keyword)) {
                return CollUtil.emptyList();
            }
            return systemUserService.queryUserIdsByUserNamePrefix(keyword);
        }
        if (TOPIC_BROADCAST.equals(topic)) {
            return systemUserService.queryAllActiveUserIds();
        }
        return CollUtil.emptyList();
    }

}
