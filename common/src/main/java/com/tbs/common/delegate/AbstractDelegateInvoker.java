package com.tbs.common.delegate;

import com.tbs.common.delegate.provider.IDelegateFlagProvider;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.function.SBiConsumer;
import com.tbs.common.function.SConsumer;
import com.tbs.common.function.SFunction;
import com.tbs.common.function.SSupplier;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tongj
 */
public abstract class AbstractDelegateInvoker<T> implements IDelegateInvoker<T> {

    IDelegateTarget<T> delegateTarget;

    @Override
    public void setTarget(IDelegateTarget<T> delegateTarget) {
        AssertUtil.notNull(delegateTarget, "delegateTarget can not be null");
        this.delegateTarget = delegateTarget;
    }

    @Data
    @AllArgsConstructor
    private static final class InvokeTask<A, B> {
        private A in;
        private B out;
        private int index;
        private boolean isIn;
    }

    AtomicInteger index = new AtomicInteger(0);

    /**
     * 获取委托执行标志提供者
     *
     * @return
     */
    protected abstract IDelegateFlagProvider getDelegateFlagProvider();

    private Queue<InvokeTask> tasks = new PriorityQueue<>(Comparator.comparing(InvokeTask::getIndex));

    @Override
    public IDelegateTarget<T> getTarget() {
        return delegateTarget;
    }

    @Override
    public <P> IDelegateInvoker<T> takeIn(SBiConsumer<T, P> method, SSupplier<P> p) {
        AssertUtil.notNull(method, "method can not be null");
        AssertUtil.notNull(p, "val provider can not be null");
        tasks.add(new InvokeTask<>(method, p, index.addAndGet(1), true));
        return this;
    }

    @Override
    public <P> IDelegateInvoker<T> takeOut(SFunction<T, P> method, SConsumer<P> consumer) {
        AssertUtil.notNull(method, "method can not be null");
        AssertUtil.notNull(consumer, "consumer can not be null");
        tasks.add(new InvokeTask<>(method, consumer, index.addAndGet(1), false));
        return this;
    }

    /**
     * 执行委托读取方法
     *
     * @param flag
     * @param method
     * @param p
     * @param <P>
     */
    protected abstract <P> void executeTakeIn(IDelegateFlag<T> flag, SBiConsumer<T, P> method, SSupplier<P> p);

    /**
     * 执行委托写入方法
     *
     * @param flag
     * @param method
     * @param consumer
     * @param <P>
     */
    protected abstract <P> void executeTakeOut(IDelegateFlag<T> flag, SFunction<T, P> method, SConsumer<P> consumer);

    @Override
    public IDelegateFlag<T> executeOnce() {
        IDelegateFlagProvider provider = getDelegateFlagProvider();
        IDelegateFlag<T> flag = provider.getDelegateFlag(this, getTarget());
        if (tasks.isEmpty()) {
            flag.setFinished(this);
            index.addAndGet(-1);
            return flag;
        }
        InvokeTask task = tasks.poll();
        if (task.isIn) {
            executeTakeIn(flag, (SBiConsumer<T, Object>)task.in, (SSupplier<Object>)task.out);
        } else {
            executeTakeOut(flag, (SFunction<T, Object>)task.in, (SConsumer<Object>)task.out);
        }

        return flag;
    }

    @Override
    public Collection<IDelegateFlag<T>> executeAll() {
        List<IDelegateFlag<T>> flags = new ArrayList<>(waittingCount() + 4);
        while (!tasks.isEmpty()) {
            IDelegateFlag<T> flag = executeOnce();
            flags.add(flag);
        }
        return flags;
    }

    @Override
    public int waittingCount() {
        return tasks.size();
    }
}
