package com.tbs.sdk.auth;

import com.tbs.sdk.model.TokenModel;

/**
 * 登录令牌管理器
 * <p>
 * 线程安全持有当前登录令牌：登录成功后由{@code LoginApi}写入，
 * HTTP层经{@link #peek()}无锁读取并自动注入请求头。令牌由调用方
 * 显式清理（{@link #clear()}），SDK不实现刷新逻辑。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class TokenManager {

    /**
     * 当前令牌模型（volatile保证跨线程可见，不可变对象无锁读取）
     */
    private volatile TokenModel current;

    /**
     * 写入令牌（登录成功后调用，覆盖旧令牌）
     *
     * @param tokenModel 登录返回的令牌模型
     */
    public void set(TokenModel tokenModel) {
        this.current = tokenModel;
    }

    /**
     * 读取当前令牌模型
     *
     * @return 当前令牌模型，未登录时返回null
     */
    public TokenModel peek() {
        return current;
    }

    /**
     * 判断是否已持有令牌
     *
     * @return 持有有效令牌串返回true
     */
    public boolean hasToken() {
        TokenModel model = current;
        return model != null && model.getToken() != null && !model.getToken().trim().isEmpty();
    }

    /**
     * 清除令牌（登出或收到认证异常时调用）
     */
    public void clear() {
        this.current = null;
    }
}
