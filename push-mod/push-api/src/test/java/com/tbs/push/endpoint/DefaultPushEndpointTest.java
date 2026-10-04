package com.tbs.push.endpoint;

import com.tbs.push.api.dispatch.IPushDispatcher;
import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.api.session.ISessionRegistry;
import com.tbs.push.api.store.IPushMessageStore;
import com.tbs.push.api.transport.IPushEndpoint;
import com.tbs.push.impl.endpoint.DefaultPushEndpoint;
import com.tbs.push.impl.protocol.JsonPushCodec;
import com.tbs.push.model.frame.FrameTypeEnum;
import com.tbs.push.model.frame.PushFrame;
import com.tbs.push.model.message.PushMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DefaultPushEndpoint 信令发布单元测试
 * <p>
 * 以轻量fake验证publishSignal链路：SIGNAL消息入源、即时分发触发一次、 消息frameType为SIGNAL且payload为空，以及publish缺省DATA的向下兼容。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
class DefaultPushEndpointTest {

    /**
     * 构造被测门面
     *
     * @param store      消息源fake
     * @param dispatcher 分发器fake
     * @return 推送门面
     */
    private IPushEndpoint endpoint(RecordingStore store, CountingDispatcher dispatcher) {
        return new DefaultPushEndpoint(store, dispatcher, new JsonPushCodec(), new EmptySessionRegistry());
    }

    /**
     * publishSignal应入源SIGNAL消息并触发一次即时分发，payload保持为空
     */
    @Test
    void publishSignalShouldEnqueueSignalMessageAndDispatchOnce() {
        RecordingStore store = new RecordingStore();
        CountingDispatcher dispatcher = new CountingDispatcher();
        IPushEndpoint endpoint = endpoint(store, dispatcher);

        String id = endpoint.publishSignal("tbs.push.channel.1001", null);

        assertNotNull(id);
        assertEquals(1, store.enqueued.size());
        assertEquals(FrameTypeEnum.SIGNAL, store.enqueued.get(0).getFrameType());
        assertEquals("tbs.push.channel.1001", store.enqueued.get(0).getTopic());
        assertNull(store.enqueued.get(0).getPayload());
        assertEquals(1, dispatcher.safeDispatchCount);
    }

    /**
     * 既有publish应缺省DATA帧类型（向下兼容）
     */
    @Test
    void publishShouldDefaultToDataFrameType() {
        RecordingStore store = new RecordingStore();
        CountingDispatcher dispatcher = new CountingDispatcher();
        IPushEndpoint endpoint = endpoint(store, dispatcher);

        endpoint.publish("global.broadcast", Collections.singletonMap("k", "v"));

        assertEquals(1, store.enqueued.size());
        assertEquals(FrameTypeEnum.DATA, store.enqueued.get(0).getFrameType());
    }

    /**
     * 信令消息应可被编解码器构造为SIGNAL帧（端到端构帧验证）
     */
    @Test
    void signaledMessageShouldEncodeToSignalFrame() {
        RecordingStore store = new RecordingStore();
        IPushEndpoint endpoint = endpoint(store, new CountingDispatcher());
        endpoint.publishSignal("tbs.push.channel.1001", null);

        PushFrame frame = new JsonPushCodec().encodeMessage(store.enqueued.get(0));
        assertEquals(FrameTypeEnum.SIGNAL, frame.getType());
    }

    /**
     * 记录入源消息的内存消息源fake
     */
    private static class RecordingStore implements IPushMessageStore {

        /**
         * 已入源消息
         */
        private final List<PushMessage> enqueued = new ArrayList<>();

        @Override
        public void enqueue(PushMessage message) {
            enqueued.add(message);
        }

        @Override
        public List<PushMessage> fetchPending(int limit) {
            return Collections.emptyList();
        }

        @Override
        public void markDispatched(String messageId) {
        }

        @Override
        public int pendingCount() {
            return 0;
        }
    }

    /**
     * 统计分发触发次数的分发器fake
     */
    private static class CountingDispatcher implements IPushDispatcher {

        /**
         * dispatchDueSafely触发次数
         */
        private int safeDispatchCount;

        @Override
        public void dispatch(PushMessage message) {
        }

        @Override
        public void dispatchDue() {
        }

        @Override
        public void dispatchDueSafely() {
            safeDispatchCount++;
        }
    }

    /**
     * 空会话注册表fake
     */
    private static class EmptySessionRegistry implements ISessionRegistry {

        @Override
        public void register(IConnectionSession session) {
        }

        @Override
        public boolean unregister(String sessionId) {
            return false;
        }

        @Override
        public IConnectionSession get(String sessionId) {
            return null;
        }

        @Override
        public Collection<IConnectionSession> all() {
            return Collections.emptyList();
        }

        @Override
        public int count() {
            return 0;
        }
    }

}
