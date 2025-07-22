package com.tbs.common.delegate.provider.impl;

import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.delegate.provider.IDelegateInvokerProvider;
import com.tbs.common.util.SpringUtil;

/**
 * Spring托管的基于类型的委托执行器提供者
 *
 * @author tongj
 */
public class SpringManagedTypeBaseInvokerProvider implements IDelegateInvokerProvider {
    @Override
    public <T> IDelegateInvoker<T> makeInvoker(Class<? extends IDelegateInvoker> invokerType) {
        return SpringUtil.getBean(invokerType);
    }
}
