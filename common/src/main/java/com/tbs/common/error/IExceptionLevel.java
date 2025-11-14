package com.tbs.common.error;

import java.io.Serializable;

public interface IExceptionLevel extends Serializable {
    /**
     * 获取异常级别
     *
     * @return 异常级别
     */
    ErrorLevelEnum getLevel();
}
