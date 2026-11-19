package com.tbs.app.form;

import com.tbs.script.api.expressible.vo.ParamValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 值获取器查询/调用表单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValueGetterForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 值获取器 ID（精确匹配）
     */
    private String getterId;

    /**
     * 调用值获取器时传入的参数值（详情接口不传）
     */
    private List<ParamValue> paramValues;
}
