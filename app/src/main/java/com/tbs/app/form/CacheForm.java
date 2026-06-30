package com.tbs.app.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * 缓存清除表单
 * <p>
 * 用于接收缓存管理相关的请求参数，支持按缓存名称和键进行查询和清除操作
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@Schema(description = "缓存清除表单")
public class CacheForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 缓存名称
     */
    @Schema(description = "缓存名称")
    private String cacheName;

    /**
     * 缓存键
     */
    @Schema(description = "缓存键")
    private Object key;
}
