package com.tbs.common.util.framework;

import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.delegate.IDelegateTarget;
import com.tbs.common.delegate.IDelegateTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author abstergo
 */
@Component
public class DelegateUtil {

    private static DelegateUtil INSTANCE;
    @Resource
    private IDelegateTemplate delegateTemplate;

    public DelegateUtil() {
        INSTANCE = this;
    }

    public static DelegateUtil getInstance() {
        return INSTANCE;
    }

    /**
     * 创建一个delegate目标
     *
     * @param targetObject
     * @param <T>
     * @return
     */
    public <T> IDelegateTarget<T> delegate(T targetObject) {
        return delegateTemplate.createDelegateTarget(targetObject);
    }

    /**
     * 创建一个delegate任务
     *
     * @param targetObject
     * @param <T>
     * @return
     */
    public <T> DelegateTaskBuilder<T> delegateWithTarget(T targetObject) {
        return DelegateTaskBuilder.create(targetObject);
    }

    public static final class DelegateTaskBuilder<T> {
        private T object;

        public static <T> DelegateTaskBuilder<T> create(T val) {
            DelegateTaskBuilder<T> builder = new DelegateTaskBuilder<>();
            builder.object = val;
            return builder;
        }

        public IDelegateInvoker<T> build(Class<? extends IDelegateInvoker> invokerType) {
            IDelegateTarget<T> delegateTarget = getInstance().delegate(object);
            return getInstance().delegateTemplate.createDelegateInvoker(invokerType, delegateTarget);
        }

    }

}
