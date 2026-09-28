package com.tbs.sdk.model;

import lombok.Data;

/**
 * SSE结构化帧上行请求
 * <p>
 * 对应服务端SseUploadForm：POST /private/push/sse/upload请求体，
 * sessionId取hello帧下发的会话标识。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class FrameUploadRequest {

    /**
     * 会话标识（必填）
     */
    private String sessionId;

    /**
     * 协议帧（必填，type必填）
     */
    private PushFrame frame;

    /**
     * 构造上行请求
     *
     * @param sessionId 会话标识
     * @param frame     协议帧
     * @return 上行请求实例
     */
    public static FrameUploadRequest of(String sessionId, PushFrame frame) {
        FrameUploadRequest request = new FrameUploadRequest();
        request.setSessionId(sessionId);
        request.setFrame(frame);
        return request;
    }
}
