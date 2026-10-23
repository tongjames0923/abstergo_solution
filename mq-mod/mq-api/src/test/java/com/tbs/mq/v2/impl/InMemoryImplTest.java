package com.tbs.mq.v2.impl;

import com.tbs.mq.v2.consume.IMqConsumer;
import com.tbs.mq.v2.core.IMqContext;
import com.tbs.mq.v2.enums.MqMessageConsumeStatusEnum;
import com.tbs.mq.v2.enums.MqMessageStatusEnum;
import com.tbs.mq.v2.vo.MqConsumeVO;
import com.tbs.mq.v2.vo.MqMessageIncomeVO;
import com.tbs.mq.v2.vo.MqRecordVO;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 内存基本实现自检
 * <p>
 * 覆盖三块非平凡逻辑：记录器的建档/幂等去重/状态推进/失败留痕、抢占器的同一消息单飞互斥与释放后可重抢、
 * 注册表按负载类型路由与增删。均以最小上下文桩（仅 {@code getContextId} 有意义）驱动，无外部依赖。
 * </p>
 *
 * @author abstergo
 * @since 2.0.0
 */
class InMemoryImplTest {

    /**
     * 仅满足接口形态的上下文桩：内存实现只依赖 {@code getContextId()} 做作用域隔离
     */
    private static final class StubContext implements IMqContext<Object, String> {
        private final Long contextId;

        StubContext(Long contextId) {
            this.contextId = contextId;
        }

        @Override
        public Long getContextId() {
            return contextId;
        }

        @Override
        public void messageReceived(List<Object> messages) {
        }

        @Override
        public void ackMessage(Object message) {
        }

        @Override
        public void nakMessage(Object message) {
        }

        @Override
        public Class<Object> getMessageType() {
            return Object.class;
        }

        @Override
        public Class<String> getMessagePayloadType() {
            return String.class;
        }

        @Override
        public Integer getQueueSize() {
            return 0;
        }

        @Override
        public void afterPropertiesSet() {
        }

        @Override
        public void destroy() {
        }
    }

    /**
     * 构造主记录VO
     */
    private static MqMessageIncomeVO<Object, String> vo(String messageId, String payload) {
        return MqMessageIncomeVO.<Object, String>builder().messageId(messageId).businessId("biz").payload(payload)
            .rawMessage(new Object()).flag(MqMessageStatusEnum.RECEIVED.getCode()).build();
    }

    @Test
    void recorderShouldRecordReadAndUpdateFlag() {
        InMemoryMqRecorder<Object, String> recorder = new InMemoryMqRecorder<>();
        StubContext context = new StubContext(1L);
        MqMessageIncomeVO<Object, String> message = vo("m1", "p1");

        List<MqRecordVO<String>> results = recorder.record(context, Collections.singletonList(message));
        assertEquals(1, results.size(), "入参逐条应各回一个结果");
        assertTrue(results.get(0).isSuccess(), "建档应成功");
        assertEquals("p1", results.get(0).getBody(), "结果 body 应为业务负载");
        assertSame(message, recorder.get(context, "m1"), "get 应回查到建档的主记录");

        recorder.updateFlag(context, "m1", MqMessageStatusEnum.PROCESSING);
        assertEquals(MqMessageStatusEnum.PROCESSING.getCode(), message.getFlag(), "状态应推进为 PROCESSING");

        recorder.errorFlag(context, "m1", new IllegalStateException("boom"));
        assertEquals(MqMessageStatusEnum.FAILED.getCode(), message.getFlag(), "errorFlag 应置 FAILED 留痕");
    }

    @Test
    void recorderShouldDedupByIdempotentMessageId() {
        InMemoryMqRecorder<Object, String> recorder = new InMemoryMqRecorder<>();
        StubContext context = new StubContext(1L);
        MqMessageIncomeVO<Object, String> first = vo("dup", "first");
        MqMessageIncomeVO<Object, String> second = vo("dup", "second");

        recorder.record(context, Collections.singletonList(first));
        List<MqRecordVO<String>> again = recorder.record(context, Collections.singletonList(second));

        assertTrue(again.get(0).isSuccess(), "重复建档仍回成功（幂等）");
        assertSame(first, recorder.get(context, "dup"), "重复 messageId 应保留首条主记录");
    }

    @Test
    void recorderShouldIsolateByContextAndRejectBlankMessageId() {
        InMemoryMqRecorder<Object, String> recorder = new InMemoryMqRecorder<>();
        StubContext ctxA = new StubContext(1L);
        StubContext ctxB = new StubContext(2L);

        recorder.record(ctxA, Collections.singletonList(vo("shared", "in-A")));
        assertNull(recorder.get(ctxB, "shared"), "不同上下文作用域应隔离，不串数据");

        List<MqRecordVO<String>> blank = recorder.record(ctxA, Collections.singletonList(vo(null, "x")));
        assertFalse(blank.get(0).isSuccess(), "messageId 缺失应回建档失败（宁拒勿漏）");
    }

    @Test
    void recorderShouldAppendConsumeLedger() {
        InMemoryMqRecorder<Object, String> recorder = new InMemoryMqRecorder<>();
        StubContext context = new StubContext(1L);
        MqConsumeVO<String> row = MqConsumeVO.<String>builder().messageId("m1").consumeId(9)
            .flag(MqMessageConsumeStatusEnum.SUCCESS.getCode()).build();

        int written = recorder.recordConsume(context, new LinkedList<>(Collections.singletonList(row)));
        assertEquals(1, written, "台账应记录追加行数");
    }

    @Test
    void monopolizerShouldEnforceSingleFlightPerMessage() {
        InMemoryMqMessageMonopolizer monopolizer = new InMemoryMqMessageMonopolizer();
        StubContext context = new StubContext(1L);
        MqMessageIncomeVO<Object, String> message = vo("m1", "p1");

        assertTrue(monopolizer.monopolize(context, message), "首次抢占应成功");
        assertFalse(monopolizer.monopolize(context, message), "他人持有期间再抢占应失败");
        assertSame(message, monopolizer.release(context, message), "release 应原样返回消息");
        assertTrue(monopolizer.monopolize(context, message), "释放后应可重新抢占");
    }

    @Test
    void registryShouldRouteByPayloadType() {
        InMemoryConsumerRegistry registry = new InMemoryConsumerRegistry();
        IMqConsumer<String> consumer = new IMqConsumer<String>() {
            @Override
            public String businessId() {
                return "biz";
            }

            @Override
            public Integer consumeId() {
                return 1;
            }

            @Override
            public void consume(String message) {
            }
        };

        registry.register(consumer, String.class);
        assertEquals(1, registry.getConsumers(String.class).size(), "应按负载类型路由到已注册消费者");
        assertTrue(registry.getConsumers(Long.class).isEmpty(), "未注册类型应回空集合");

        registry.unregister(String.class, consumer);
        assertTrue(registry.getConsumers(String.class).isEmpty(), "注销后该类型应无消费者");
    }
}
