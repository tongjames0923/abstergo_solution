package com.tbs.push.sse.controller;

import com.tbs.push.model.PushFrame;
import com.tbs.push.model.PushSessionId;
import lombok.Data;

import java.io.Serializable;

/**
 * SSE上行请求表单
 * <p>
 * HTTP POST上行端点的请求体，参数全部类型化：
 * <ul>
 *     <li>sessionId — {@link PushSessionId}值对象：非空，格式为认证阶段预分配、
 *     经握手确认帧下发的会话唯一标识（UUID形态）</li>
 *     <li>frame — {@link PushFrame}结构化协议帧：type为{@link com.tbs.push.model.FrameTypeEnum}
 *     枚举（必填，取值HELLO/PING/PONG/DATA/SUBSCRIBE/UNSUBSCRIBE/ERROR），version为协议版本号
 *     （当前为{@link com.tbs.push.constants.PushFrameConstants#PROTOCOL_VERSION}=1），
 *     topic为{@link com.tbs.push.model.PushTopic}值对象（SUBSCRIBE/UNSUBSCRIBE/DATA帧必填，
 *     非空且不超过128字符），payload为帧载荷文本</li>
 * </ul>
 * 相比历史版本的协议帧JSON字符串参数，本结构让格式约束在类型上显式表达，
 * 非法取值在HTTP绑定/校验阶段即被拒绝。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class SseUploadForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标会话唯一标识（连接建立时由握手确认帧下发），非空
     */
    private PushSessionId sessionId;

    /**
     * 结构化协议帧，必填；type必填，topic按帧类型要求，version应为当前协议版本
     */
    private PushFrame frame;

}
