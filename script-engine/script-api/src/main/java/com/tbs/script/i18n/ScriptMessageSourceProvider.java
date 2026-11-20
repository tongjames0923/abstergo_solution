package com.tbs.script.i18n;

import com.tbs.common.i18n.AbstractMessageSourceProvider;
import org.springframework.stereotype.Component;

/**
 * 脚本引擎模块消息源提供者。
 *
 * <p>注册 script 模块的国际化资源文件（messages-script），供断言异常与触发器展示文案统一取用，
 * 随 {@code com.tbs.script} 包扫描生效。</p>
 */
@Component
public class ScriptMessageSourceProvider extends AbstractMessageSourceProvider {

    /**
     * 获取消息资源基础名称。
     *
     * @return 资源文件基础名称
     */
    @Override
    public String getBasename() {
        return "i18n/messages-script";
    }

    /**
     * 获取消息源优先级。
     *
     * @return 优先级，数字越小越靠前
     */
    @Override
    public int getOrder() {
        return 100;
    }
}
