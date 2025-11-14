package com.tbs.common.error;

import com.tbs.common.exception.FormattedRuntimeException;
import org.springframework.core.Ordered;

/**
 * 异常转换接口
 *
 * @author tongj
 */
public interface IErrorTransformer extends Ordered {
    /**
     * 是否支持该异常转换
     *
     * @param throwable
     * @return
     */
    boolean support(Throwable throwable);

    /**
     * 转换异常
     *
     * @param throwable
     * @return
     */
    FormattedRuntimeException toFormattedRuntimeException(Throwable throwable);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
