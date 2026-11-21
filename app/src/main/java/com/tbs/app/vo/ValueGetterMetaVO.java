package com.tbs.app.vo;

import com.tbs.script.api.expressible.vo.param.ParamType;
import com.tbs.script.api.expressible.vo.result.ResultType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 值获取器元信息，用于前端展示与脚本编辑器建模
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValueGetterMetaVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 值获取器 ID
     */
    private String getterId;

    /**
     * 值获取器名称
     */
    private String getterName;

    /**
     * 值获取器描述
     */
    private String description;

    /**
     * 请求参数定义（含枚举值、是否必填、排序）
     */
    private List<ParamType> paramTypes;

    /**
     * 返回值类型定义（含描述、是否数组）
     */
    private ResultType resultType;
}
