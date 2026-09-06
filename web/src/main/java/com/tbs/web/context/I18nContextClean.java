package com.tbs.web.context;

import com.tbs.common.i18n.I18nContextHolder;
import org.springframework.stereotype.Component;

/**
 * 国际化语言上下文清理器
 * <p>
 * 清理 {@link I18nContextHolder} 持有的语言上下文。语言上下文由 {@code I18nFilter}（HTTP 入口解析）与 {@code I18nInterceptor}（AOP 层备份）
 * 随请求设置，统一经本清理器在请求生命周期终点清理， 防止容器线程复用导致语言串台与内存泄漏。
 * </p>
 *
 * @author abstergo
 * @see IContextClean
 * @see I18nContextHolder
 */
@Component
public class I18nContextClean implements IContextClean {

    @Override
    public void clean() {
        I18nContextHolder.clear();
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
