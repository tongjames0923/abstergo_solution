package com.tbs.common.holder.context;

/**
 * 抽象命名上下文
 *
 * @author abstergo
 */
public abstract class AbstractNamedContext implements IContext {
    private String contextId;
    private boolean valid = true;

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
}
