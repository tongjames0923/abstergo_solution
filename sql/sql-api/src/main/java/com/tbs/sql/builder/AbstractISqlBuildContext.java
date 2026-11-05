package com.tbs.sql.builder;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL构建上下文抽象实现类，提供上下文接口的通用实现。
 *
 * <p>该类实现了 {@link ISqlBuildContext} 接口，提供了SQL参数管理、上下文状态控制、
 * 实体对象管理等通用功能。子类只需要提供实体对象的创建逻辑。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>管理SQL参数 {@link #getSqlParams()}</li>
 *   <li>控制上下文生命周期 {@link #destroy()} {@link #rebuild()}</li>
 *   <li>管理实体对象 {@link #getSqlEntity()}</li>
 *   <li>验证上下文有效性 {@link #isValid()}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>作为SQL构建上下文的基类使用</li>
 *   <li>需要自定义SQL上下文逻辑时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * public class CustomSqlBuildContext extends AbstractSqlBuildContext {
 *     @Override
 *     protected Object provideSqlEntity() {
 *         return new CustomEntity();
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>子类必须实现 {@link #provideSqlEntity()} 方法</li>
 *   <li>上下文销毁后不应再使用</li>
 *   <li>重建上下文会重置所有状态</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see ISqlBuildContext
 * @see ImportedISqlBuildContext
 */
public abstract class AbstractISqlBuildContext implements ISqlBuildContext {
    private Map<String, Object> sqlParams = new HashMap<>(4);
    private boolean valid = true;
    private Object sqlEntity;
    private Class<?> sqlEntityClass;
    private Map<String, Object> context = new HashMap<>(4);

    @Override
    public Class<?> getEntityClass() { return sqlEntityClass; }

    /**
     * 提供实体对象
     *
     * @return 实体对象
     */
    protected abstract Object provideSqlEntity();

    protected void clearEntity() { sqlEntity = null; sqlEntityClass = null; }

    @Override
    public Object getSqlEntity() { return sqlEntity; }

    @Override
    public Map<String, Object> getSqlParams() { return sqlParams; }

    @Override
    public void destroy() {
        sqlParams.clear();
        sqlEntity = null;
        sqlEntityClass = null;
        valid = false;
        sqlParams.clear();
        context.clear();
    }

    @Override
    public void rebuild() {
        destroy();
        sqlEntity = provideSqlEntity();
        sqlEntityClass = sqlEntity.getClass();
        valid = true;
    }

    @Override
    public boolean isValid() { return valid; }

    @Override
    public Map<String, Object> getContextParams() {
        return context;
    }
}
