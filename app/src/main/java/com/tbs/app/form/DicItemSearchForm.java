package com.tbs.app.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据字典项查询表单
 * <p>
 * 用于接收查询数据字典项的请求参数，支持按字典编码和字典项标签进行筛选
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@Schema(description = "数据字典项查询表单")
public class DicItemSearchForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字典编码
     */
    @Schema(description = "字典编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dicCode;

    /**
     * 字典项标签
     */
    @Schema(description = "字典项标签", requiredMode = Schema.RequiredMode.REQUIRED)
    private String label;
}
