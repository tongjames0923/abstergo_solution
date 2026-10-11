package com.tbs.sdk.http;

import com.tbs.sdk.constants.SdkConstants;
import com.tbs.sdk.exception.SdkAuthException;
import com.tbs.sdk.exception.SdkException;
import com.tbs.sdk.exception.SdkReloginRequiredException;
import com.tbs.sdk.exception.SdkServerException;

/**
 * 服务端统一响应载体（内部模型）
 * <p>
 * 对应服务端AjaxResult的Map结构（键：code/message/data）， 由HTTP实现层统一解析并按业务码映射SDK异常，业务调用方仅接触data反序列化结果。
 * data经泛型参数{@code <T>}承载目标类型：解析阶段为原始JSON节点，
 * 完成data反序列化后即为强类型结果，类型约束随响应对象全链路传递。
 * </p>
 *
 * @param <T> data节点目标类型
 * @author abstergo
 * @since 1.0.0
 */
public class AjaxResponse<T> {

    /**
     * 业务码
     */
    private final int code;

    /**
     * 服务端i18n消息
     */
    private final String message;

    /**
     * data节点（泛型承载目标类型，可能为null）
     */
    private final T data;

    /**
     * 构造统一响应载体
     *
     * @param code    业务码
     * @param message 服务端消息
     * @param data    data节点（类型由调用方约束）
     */
    AjaxResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 判断业务码是否成功
     *
     * @return 成功返回true
     */
    public boolean isOk() {
        return code == SdkConstants.CODE_OK;
    }

    /**
     * 获取业务码
     *
     * @return 业务码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取服务端i18n消息
     *
     * @return 消息文本
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取data节点
     *
     * @return data节点（类型为泛型目标类型），缺失时为null
     */
    public T getData() {
        return data;
    }

    /**
     * 判断业务码是否代表会话失效（405令牌失效 / 468刷新失败）
     * <p>
     * 该类码在 App/SDK 模式一律要求宿主重新登录，HTTP层据此先行触发会话失效事件。
     * </p>
     *
     * @param code 业务码
     * @return 会话失效返回true
     */
    public static boolean isSessionExpired(int code) {
        return code == SdkConstants.CODE_RELOGIN || code == SdkConstants.CODE_REFRESH_FAIL;
    }

    /**
     * 按业务码映射SDK异常
     * <p>
     * 405/468归入会话失效重登异常（{@link SdkReloginRequiredException}，其父类为认证异常），
     * 401归入认证异常，其余非成功码归入服务端异常；成功码返回null。
     * </p>
     *
     * @return 对应异常，成功时为null
     */
    public SdkException mapToException() {
        if (isOk()) {
            return null;
        }
        if (isSessionExpired(code)) {
            return new SdkReloginRequiredException(code, message);
        }
        if (code == SdkConstants.CODE_UNAUTHORIZED) {
            return new SdkAuthException(code, message);
        }
        return new SdkServerException(code, message);
    }
}
