package com.tbs.app.form;

import com.tbs.script.api.expressible.vo.param.ParamValue;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 动作测试执行表单。
 *
 * @author abstergo
 */
@Data
public class EventActionTestForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 动作ID
     */
    private String actionId;

    /**
     * 动作参数值
     */
    private List<ParamValue> paramValues;
}
