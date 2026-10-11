package com.tbs.cdc.listener;

import com.tbs.cdc.model.CdcChangeType;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.Set;

/**
 * 变更事件订阅描述
 * <p>
 * 编程式监听器{@link CdcChangeListener}通过本结构声明其关注范围。路由匹配规则：
 * 库名与表名正则均为空表示不限制；变更类型集合为空表示订阅全部类型。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@Builder
public class CdcSubscription {

    /**
     * 关注的数据库（schema）名称，为空表示不限制库
     */
    private String database;

    /**
     * 关注表的正则表达式集合，为空表示不限制表
     */
    private Set<String> tablePatterns;

    /**
     * 关注的变更类型集合，为空表示订阅全部类型
     */
    private Set<CdcChangeType> changeTypes;

    /**
     * 是否以异步方式执行监听
     * <p>
     * true表示提交到独立线程池执行，慢监听不阻塞捕获链路；false表示同步直发。
     * </p>
     */
    @Builder.Default
    private boolean async = true;

    /**
     * 构造一个订阅全部变更的默认描述
     *
     * @return 不限制库表与类型、异步执行的订阅描述
     */
    public static CdcSubscription all() {
        return CdcSubscription.builder()
            .database(null)
            .tablePatterns(Collections.emptySet())
            .changeTypes(Collections.emptySet())
            .async(true)
            .build();
    }
}
