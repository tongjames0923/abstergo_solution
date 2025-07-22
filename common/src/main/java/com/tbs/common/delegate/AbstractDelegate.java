package com.tbs.common.delegate;

import com.tbs.common.error.AssertUtil;

/**
 * 抽象委托类
 *
 * @author abstergo
 */
public abstract class AbstractDelegate<T> implements IDelegate<T> {
    private T target;

    /**
     * 构造委托
     *
     * @param target
     */
    public AbstractDelegate(T target) {
        AssertUtil.notNull(target, "委托不能为空");
        this.target = target;
    }

    @Override
    public T getTarget() {
        return target;
    }

    @Override
    public void reset(T target) {
        AssertUtil.notNull(target, "委托不能为空");
        this.target = target;
    }
}
