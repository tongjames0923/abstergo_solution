package com.tbs.schedule.i18n;

import com.tbs.common.i18n.AbstractMessageSourceProvider;
import org.springframework.stereotype.Component;

/**
 * 调度模块消息源提供者
 * <p>
 * 注册 async 模块下 schedule 包的国际化资源文件（messages-schedule），供断言异常文案统一取用。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
public class ScheduleMessageSourceProvider extends AbstractMessageSourceProvider {

    @Override
    public String getBasename() {
        return "i18n/messages-schedule";
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
