package com.tbs.push.model.session;

import lombok.Data;

import java.io.Serializable;

/**
 * 传输能力声明
 * <p>
 * 描述一个传输适配层具备的通道能力，供上层在装配期做能力校验： 能力不满足时快速失败（断言异常），避免运行期出现语义不符的投递行为。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class TransportCapability implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否支持客户端向服务端上行发送帧
     * <p>
     * WebSocket为原生双向；SSE经HTTP POST上行组合实现，同样声明为true。
     * </p>
     */
    private boolean upstreamSupported;

    /**
     * 是否支持二进制载荷
     */
    private boolean binarySupported;

    /**
     * 构造传输能力声明
     *
     * @param upstreamSupported 是否支持上行
     * @param binarySupported   是否支持二进制载荷
     */
    public TransportCapability(boolean upstreamSupported, boolean binarySupported) {
        this.upstreamSupported = upstreamSupported;
        this.binarySupported = binarySupported;
    }

    /**
     * 原生双向通道能力（上行原生支持，不支持二进制）
     *
     * @return 能力声明
     */
    public static TransportCapability bidirectionalText() {
        return new TransportCapability(true, false);
    }

}
