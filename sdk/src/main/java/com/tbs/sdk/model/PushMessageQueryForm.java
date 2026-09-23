package com.tbs.sdk.model;

import lombok.Data;

/**
 * 消息列表查询表单
 * <p>
 * 对应服务端PushMessageQueryForm：游标分页（cursor为上一页响应的游标），
 * 到达序倒序、含已读。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushMessageQueryForm {

    /**
     * 已读筛选：全部
     */
    public static final String READ_FLAG_ALL = "all";

    /**
     * 已读筛选：仅未读
     */
    public static final String READ_FLAG_UNREAD = "unread";

    /**
     * 翻页游标（首页不传）
     */
    private Long cursor;

    /**
     * 单页条数（缺省20，上限100）
     */
    private Integer limit;

    /**
     * 已读筛选（all/unread，缺省all）
     */
    private String readFlag;
}
