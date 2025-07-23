package com.tbs.common.delegate;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;

/**
 * 代理执行标记
 *
 * @author tongj
 */
public interface IDelegateFlag<T> {

    /**
     * 获取代理执行对象
     *
     * @return
     */
    IDelegateTarget<T> get();

    /**
     * 等待完成
     *
     * @return
     */
    IDelegateFlag<T> waitUntilFinished();

    /**
     * 设置完成
     *
     * @param invoker
     * @return
     */
    boolean setFinished(IDelegateInvoker<T> invoker);

    /**
     * 设置异常
     *
     * @param e
     * @param invoker
     * @return
     */
    boolean setException(Throwable e, IDelegateInvoker<T> invoker);

    /**
     * 获取异常
     *
     * @return
     */
    Throwable getException();

    /**
     * 是否完成
     *
     * @return
     */
    boolean isFinished();

    /**
     * 异常时抛出
     *
     * @return
     * @throws Throwable
     */
    default IDelegateFlag<T> expceptionThenThrow() throws FormattedRuntimeException {
        if (getException() != null) {
            AssertUtil.throwError(() -> new FormattedRuntimeException(getException(), "delegate error"));
        }
        return this;
    }
}
