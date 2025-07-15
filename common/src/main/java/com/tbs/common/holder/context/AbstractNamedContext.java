package com.tbs.common.holder.context;

/**
 * 抽象命名上下文
 *
 * @author abstergo
 */
public abstract class AbstractNamedContext<V> implements IContext<V> {
    private String contextId;
    private boolean valid = true;

    /**
     * 构造函数
     *
     * @param contextId 上下文ID
     */
    public AbstractNamedContext(String contextId) {
        this.contextId = contextId;
    }

    /**
     * 令上下文失效
     */
    protected void invalidate() {
        this.valid = false;
    }

    /**
     * 令上下文有效
     */
    protected void validate() {
        this.valid = true;
    }

    /**
     * 重建上下文
     *
     * @return 新的上下文
     */
    protected IContext<V> rebuild() {
        return this;
    }

    /**
     * 重置上下文ID
     */
    protected void resetContextId(String contextId) {
        this.contextId = contextId;
    }

    @Override
    public String getContextId() {
        return this.contextId;
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }

    @Override
    public IContext<V> rebuildContext() {
        return rebuild();
    }

    @Override
    public void markInvalid() {
        invalidate();
    }
}
