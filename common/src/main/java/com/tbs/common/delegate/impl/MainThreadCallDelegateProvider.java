package com.tbs.common.delegate.impl;

import com.tbs.common.delegate.IDelegate;
import com.tbs.common.delegate.IDelegateProvider;

/**
 * 主线程调用代理提供者
 *
 * @author abstergo
 */
public class MainThreadCallDelegateProvider implements IDelegateProvider {
    @Override
    public <T> IDelegate<T> getDelegate(T val) {
        return new MainThreadCallDelegate<T>(val);
    }
}
