package com.tbs.push.sse.controller;

import lombok.Data;

import java.io.Serializable;

/**
 * SSE上行请求表单
 * <p>
 * HTTP POST上行端点的请求体：携带目标会话标识与协议帧JSON文本。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class SseUploadForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标会话唯一标识（连接建立时由握手确认帧下发）
     */
    private String sessionId;

    /**
     * 协议帧JSON文本
     */
    private String frame;

}
