package com.tbs.app.form;

import lombok.Data;

import java.io.Serializable;

/**
 * 下载码分页查询条件表单
 * <p>
 * HTTP层专属查询入参：码精确匹配、对象键前缀、关键字模糊与状态筛选；
 * 分页参数 pageNum/pageSize 依接口规范走 query 参数，不在本表单内
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class OssDownloadCodeSearchForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 精确下载码
     */
    private String code;

    /**
     * 存储对象键前缀
     */
    private String objectKey;

    /**
     * 关键字（匹配码/对象键/备注）
     */
    private String keyword;

    /**
     * 状态筛选：VALID有效 / EXPIRED已过期 / PERMANENT永久有效，空表示全部
     */
    private String status;
}
