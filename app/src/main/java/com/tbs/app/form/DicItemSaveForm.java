package com.tbs.app.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据字典项保存表单
 * <p>
 * 用于接收保存数据字典项的请求参数，包含字典编码、字典项标签和字典项值
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@Schema(description = "数据字典项保存表单")
public class DicItemSaveForm implements Serializable {
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
    private String dicItemLabel;

    /**
     * 字典项值
     */
    @Schema(description = "字典项值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dicItemValue;
}
