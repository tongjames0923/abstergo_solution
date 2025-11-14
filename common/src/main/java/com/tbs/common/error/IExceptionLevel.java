package com.tbs.common.error;

import java.io.Serializable;

/**
 * 异常级别接口
 * <p>
 * 该接口定义了获取异常级别的方法，实现该接口的类需要提供具体的异常级别信息。 通过实现该接口，可以统一管理系统中各种异常的级别。
 * </p>
 */
public interface IExceptionLevel extends Serializable {
    /**
     * 获取异常级别
     *
     * @return 异常级别
     */
    ErrorLevelEnum getLevel();
}
