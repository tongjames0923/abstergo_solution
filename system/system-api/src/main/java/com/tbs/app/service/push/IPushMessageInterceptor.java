package com.tbs.app.service.push;

import com.tbs.app.form.PushNotifySendForm;
import org.springframework.core.Ordered;

/**
 * 消息拦截器
 */
public interface IPushMessageInterceptor extends Ordered {
    /**
     * 拦截消息
     *
     * @param message 消息
     * @return 拦截后消息
     */
    PushNotifySendForm intercept(PushNotifySendForm message);

    default boolean support(PushNotifySendForm message) {
        return false;
    }

    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
