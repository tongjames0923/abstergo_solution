package com.tbs.app.form;

import lombok.Data;

import java.io.Serializable;

/**
 * 推送消息分页拉取查询表单
 * <p>
 * 消息中心列表查询入参：游标分页（cursor 为上一页响应返回的接收行ID）， 到达序倒序、含已读；readFlag 仅作为列表筛选条件。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushMessageQueryForm implements Serializable {

    /**
     * 读状态筛选标记：全部
     */
    public static final String READ_FLAG_ALL = "all";
    /**
     * 读状态筛选标记：仅未读
     */
    public static final String READ_FLAG_UNREAD = "unread";
    private static final long serialVersionUID = 1L;
    /**
     * 游标（上一页最后一行的接收行ID，首页不传）
     */
    private Long cursor;

    /**
     * 每页条数（缺省20，上限100）
     */
    private Integer limit;

    /**
     * 读状态筛选（all/unread，缺省 all）
     */
    private String readFlag;

}
