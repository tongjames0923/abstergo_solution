package com.tbs.push.impl.endpoint;

import com.tbs.common.error.AssertUtil;
import com.tbs.push.api.dispatch.IPushDispatcher;
import com.tbs.push.api.frame.IPushCodec;
import com.tbs.push.api.session.ISessionRegistry;
import com.tbs.push.api.store.IPushMessageStore;
import com.tbs.push.api.transport.IPushEndpoint;
import com.tbs.push.constants.PushI18nConstant;
import com.tbs.push.model.frame.FrameTypeEnum;
import com.tbs.push.model.message.PushMessage;

import java.util.Map;
import java.util.UUID;

/**
 * 默认推送统一门面
 * <p>
 * 业务侧唯一入口的默认实现：发布即入源（消息源承担暂存与削峰）， 随后触发一轮即时分发尝试（实时性优先，未取到的消息由调度驱动兜底补发一轮）。 业务方完全不感知传输、订阅与投递细节。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DefaultPushEndpoint implements IPushEndpoint {

    /**
     * 消息源
     */
    private final IPushMessageStore messageStore;

    /**
     * 消息分发器
     */
    private final IPushDispatcher dispatcher;

    /**
     * 协议编解码器
     */
    private final IPushCodec codec;

    /**
     * 会话注册表
     */
    private final ISessionRegistry sessionRegistry;

    /**
     * 构造默认推送门面
     *
     * @param messageStore    消息源，不能为空
     * @param dispatcher      消息分发器，不能为空
     * @param codec           协议编解码器，不能为空
     * @param sessionRegistry 会话注册表，不能为空
     */
    public DefaultPushEndpoint(IPushMessageStore messageStore, IPushDispatcher dispatcher, IPushCodec codec,
        ISessionRegistry sessionRegistry) {
        AssertUtil.isTrue(messageStore == null).i18n(PushI18nConstant.MESSAGE_STORE_REQUIRED).throwIt();
        AssertUtil.isTrue(codec == null).i18n(PushI18nConstant.CODEC_REQUIRED).throwIt();
        AssertUtil.isTrue(sessionRegistry == null).i18n(PushI18nConstant.SESSION_REGISTRY_REQUIRED).throwIt();
        this.messageStore = messageStore;
        this.dispatcher = dispatcher;
        this.codec = codec;
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * 发布业务消息（自动生成消息标识）
     *
     * @param topic   业务主题
     * @param payload 业务载荷对象
     * @return 消息唯一标识
     */
    @Override
    public String publish(String topic, Object payload) {
        return publish(topic, payload, null);
    }

    /**
     * 发布业务消息（自动生成消息标识，附元数据）
     *
     * @param topic    业务主题
     * @param payload  业务载荷对象
     * @param metadata 消息元数据
     * @return 消息唯一标识
     */
    @Override
    public String publish(String topic, Object payload, Map<String, String> metadata) {
        PushMessage message = new PushMessage();
        message.setId(UUID.randomUUID().toString().replace("-", ""));
        message.setTopic(topic);
        message.setPayload(codec.serializePayload(payload));
        message.setMetadata(metadata);
        return publish(message);
    }

    /**
     * 发布已构造好的业务消息并触发一轮即时分发
     *
     * @param message 业务消息
     * @return 消息唯一标识
     */
    @Override
    public String publish(PushMessage message) {
        messageStore.enqueue(message);
        dispatcher.dispatchDueSafely();
        return message.getId();
    }

    /**
     * 发布信令消息（SIGNAL帧，复用入源与即时分发链路）
     * <p>
     * 信令与业务消息走同一条入源-分发链路，仅帧类型不同：编解码器按frameType构帧， 传输层与订阅匹配完全复用。payload允许为空（信令不承载业务内容）。
     * </p>
     *
     * @param topic   目标频道主题
     * @param payload 信令载荷文本，允许为null
     * @return 帧消息唯一标识
     */
    @Override
    public String publishSignal(String topic, String payload) {
        PushMessage message = new PushMessage();
        message.setId(UUID.randomUUID().toString().replace("-", ""));
        message.setTopic(topic);
        message.setFrameType(FrameTypeEnum.SIGNAL);
        message.setPayload(payload);
        return publish(message);
    }

    /**
     * 统计全部在线会话数量
     *
     * @return 在线会话数量
     */
    @Override
    public int onlineCount() {
        return sessionRegistry.count();
    }

}
