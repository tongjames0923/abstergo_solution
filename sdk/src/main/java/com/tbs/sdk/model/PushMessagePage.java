package com.tbs.sdk.model;

import java.util.List;
import lombok.Data;

/**
 * 消息分页结果
 * <p>
 * 对应服务端PushMessagePageVO：cursor为下一页游标（无更多数据时为null），
 * 调用方以hasMore判断是否继续翻页。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushMessagePage {

    /**
     * 当前页消息列表
     */
    private List<PushNotifyMessage> list;

    /**
     * 下一页游标（无更多数据时为null）
     */
    private Long cursor;

    /**
     * 是否还有更多数据
     */
    private Boolean hasMore;
}
