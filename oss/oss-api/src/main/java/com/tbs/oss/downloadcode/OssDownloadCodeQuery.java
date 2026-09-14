package com.tbs.oss.downloadcode;

import lombok.Builder;
import lombok.Data;

/**
 * 下载码查询条件模型
 * <p>
 * 存储抽象层的查询入参：精确码、对象键前缀、关键字（码/对象键/备注模糊）、
 * 状态筛选与分页偏移；各存储实现负责将条件翻译为自身查询语义
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@Builder
public class OssDownloadCodeQuery {

    /**
     * 精确下载码（可选）
     */
    private String code;

    /**
     * 存储对象键前缀（可选）
     */
    private String objectKey;

    /**
     * 关键字（可选，匹配码/对象键/备注）
     */
    private String keyword;

    /**
     * 状态筛选（可选，空表示全部）
     */
    private OssDownloadCodeStatus status;

    /**
     * 分页起始偏移（从0开始）
     */
    private int offset;

    /**
     * 每页条数（小于等于0表示不分页，取全部）
     */
    private int limit;
}
