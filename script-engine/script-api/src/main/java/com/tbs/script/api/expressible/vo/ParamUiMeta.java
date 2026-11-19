package com.tbs.script.api.expressible.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 参数输入控件元信息。
 *
 * <p>只描述参数在前端如何呈现与校验，不参与取值语义；取值语义仍由 {@link ParamType} 的
 * type、required、isEnum 与 enumValues 表达。</p>
 *
 * @author abstergo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParamUiMeta implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 输入控件类型，取值 text、textarea、number、boolean、date、datetime、time、select、json、operand；
     * 为空时前端按 ParamType.type 推断
     */
    private String inputType;

    /**
     * 输入占位提示
     */
    private String placeholder;

    /**
     * 默认值，字符串形式，由前端解析为控件值
     */
    private String defaultValue;

    /**
     * 是否可搜索，仅 select 控件有效
     */
    private Boolean filterable;

    /**
     * 是否允许自定义输入，为 true 时 enumValues 作为候选而非约束
     */
    private Boolean allowCreate;

    /**
     * 文本最大长度
     */
    private Integer maxLength;

    /**
     * 文本校验正则
     */
    private String pattern;

    /**
     * 数值下限
     */
    private BigDecimal min;

    /**
     * 数值上限
     */
    private BigDecimal max;

    /**
     * 数值步进
     */
    private BigDecimal step;

}
