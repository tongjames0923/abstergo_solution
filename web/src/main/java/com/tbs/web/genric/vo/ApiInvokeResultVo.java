package com.tbs.web.genric.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 泛化接口调用结果视图
 * <p>
 * 承载单次泛化调用的整体执行信息：耗时区间、成功标志（仅 {@code 1} 表示成功）、异常明细，以及执行返回结果。
 * 由 {@link com.tbs.web.genric.AbstractGenricApiInstance#invokeApi} 填充，便于前端展示与排错。
 * </p>
 *
 * @param <R> 执行返回结果的序列化类型
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiInvokeResultVo<R extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 开始时间
     */
    private Date beginTime;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 是否成功,仅有1的时候是执行成功
     */
    private Integer successFlag;
    /**
     * 异常类名
     */
    private String exceptionClassName;
    /**
     * 异常信息
     */
    private String exceptionMessage;
    /**
     * 异常对象
     */
    private Serializable exceptionObject;
    /**
     * 执行结果
     */
    private R executeResult;
}
