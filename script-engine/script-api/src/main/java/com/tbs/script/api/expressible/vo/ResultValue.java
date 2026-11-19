package com.tbs.script.api.expressible.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultValue implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 结果类型
     */
    private ResultType type;

    /**
     * 结果值
     */
    private Serializable value;

    /**
     * 结果值集合
     */
    private Collection<Serializable> values;
}
