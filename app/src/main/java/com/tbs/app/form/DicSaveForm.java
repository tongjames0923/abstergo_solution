package com.tbs.app.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据字典保存表单
 * <p>
 * 用于接收保存数据字典的请求参数，包含字典编码和字典名称
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@Schema(description = "数据字典保存表单")
public class DicSaveForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字典编码
     */
    @Schema(description = "字典编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dicCode;

    /**
     * 字典名称
     */
    @Schema(description = "字典名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dicName;
}
