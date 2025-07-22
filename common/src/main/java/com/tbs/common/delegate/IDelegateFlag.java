package com.tbs.common.delegate;

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
    IDelegateTarget<T> getUnitlFinished();

    /**
     * 设置完成
     *
     * @param invoker
     */
    void setFinished(IDelegateInvoker<T> invoker);

    /**
     * 是否完成
     *
     * @return
     */
    boolean isFinished();
}
