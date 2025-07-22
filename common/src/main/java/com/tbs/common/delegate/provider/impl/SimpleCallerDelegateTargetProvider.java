package com.tbs.common.delegate.provider.impl;

import com.tbs.common.delegate.IDelegateTarget;
import com.tbs.common.delegate.impl.CallerRunsDelegateTarget;
import com.tbs.common.delegate.provider.IDelegateTargetProvider;

/**
 * 默认委托者提供者
 *
 * @author tongj
 */
public class SimpleCallerDelegateTargetProvider implements IDelegateTargetProvider {
    @Override
    public <T> IDelegateTarget<T> getDelegateTarget(T targetObject) {
        return new CallerRunsDelegateTarget<T>(targetObject);
    }
}
