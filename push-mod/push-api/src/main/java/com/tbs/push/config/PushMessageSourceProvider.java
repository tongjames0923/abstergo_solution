package com.tbs.push.config;

import com.tbs.common.i18n.AbstractMessageSourceProvider;
import org.springframework.stereotype.Component;

/**
 * 推送模块消息源提供者
 * <p>
 * 自动注册推送协议层的消息资源文件：i18n/messages-push.properties。
 * 资源文件随协议层代码一同分发。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
public class PushMessageSourceProvider extends AbstractMessageSourceProvider {

    /**
     * 获取资源文件basename
     *
     * @return 资源文件basename
     */
    @Override
    public String getBasename() {
        return "i18n/messages-push";
    }

    /**
     * 获取优先级
     *
     * @return 优先级，与相邻模块错开
     */
    @Override
    public int getOrder() {
        // 推送模块优先级，与view-route(-600)/rabbitmq(-500)错开
        return -450;
    }

}
