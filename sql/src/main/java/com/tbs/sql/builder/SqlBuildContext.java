package com.tbs.sql.builder;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL构建上下文接口，定义了SQL构建过程中的上下文环境。
 *
 * <p>该接口封装了SQL构建过程中需要的所有信息，包括实体类、SQL实体对象、
 * SQL参数等。上下文对象在整个SQL构建和执行过程中传递和共享数据。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>提供实体类信息</li>
 *   <li>提供SQL实体对象</li>
 *   <li>管理SQL参数</li>
 *   <li>控制上下文生命周期</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在SQL构建过程中传递上下文信息</li>
 *   <li>在SQL驱动中获取实体和参数信息</li>
 *   <li>在拦截器中访问和修改上下文数据</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 获取上下文中的实体类
 * Class<?> entityClass = context.getEntityClass();
 * 
 * // 获取SQL参数
 * Map<String, Object> params = context.getSqlParams();
 * 
 * // 销毁上下文，释放资源
 * context.destroy();
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>上下文对象应该在使用完毕后及时销毁</li>
 *   <li>不要在多个线程间共享同一个上下文对象</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see AbstractSqlBuildContext
 * @see ImportedSqlBuildContext
 */
public interface SqlBuildContext {
    /**
     * 获取实体类类型。
     *
     * @return 实体类类型
     */
    Class<?> getEntityClass();

    /**
     * 获取SQL实体对象。
     *
     * @return SQL实体对象
     */
    Object getSqlEntity();

    /**
     * 获取SQL参数映射。
     *
     * @return SQL参数映射，键为参数名，值为参数值
     */
    Map<String, Object> getSqlParams();

    /**
     * 判断上下文是否有效。
     *
     * @return 如果上下文有效返回true，否则返回false
     */
    boolean isValid();

    /**
     * 销毁上下文，释放资源。
     */
    void destroy();

    /**
     * 重建上下文，重置所有状态。
     */
    void rebuild();
}
