package com.tbs.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 推送消息分页拉取结果视图
 * <p>
 * 游标分页响应体：cursor 为下一页查询入参（本页最后一行的接收行ID）， hasMore 依据本页取数是否触及批次上界判定。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushMessagePageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 本页消息列表
     */
    private List<PushNotifyMessageVO> list;

    /**
     * 下一页游标（本页最后一行的接收行ID，无更多数据时为null）
     */
    private Long cursor;

    /**
     * 是否还有更多数据
     */
    private Boolean hasMore;

}
