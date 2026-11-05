package com.tbs.sql.builder;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.function.SSupplier;

/**
 * 导入式SQL构建上下文实现类，通过供应商函数动态提供SQL实体对象。
 *
 * <p>该类继承自 {@link AbstractISqlBuildContext}，通过构造函数接收一个供应商函数，
 * 用于动态创建SQL实体对象。这种方式提供了更灵活的实体对象创建机制。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>通过供应商函数动态创建SQL实体对象</li>
 *   <li>支持延迟加载和按需创建</li>
 *   <li>提供灵活的实体对象创建机制</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要动态创建SQL实体对象时</li>
 *   <li>实体对象创建逻辑复杂时</li>
 *   <li>需要延迟加载实体对象时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建供应商函数
 * SSupplier<Object> supplier = () -> new User();
 *
 * // 创建上下文
 * ImportedSqlBuildContext context = new ImportedSqlBuildContext(supplier);
 *
 * // 重建上下文，会重新调用供应商函数创建实体
 * context.rebuild();
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>供应商函数不能为null</li>
 *   <li>每次重建上下文都会调用供应商函数</li>
 *   <li>确保供应商函数线程安全</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see AbstractISqlBuildContext
 * @see ISqlBuildContext
 */
public class ImportedISqlBuildContext extends AbstractISqlBuildContext {
    private SSupplier<Object> sqlEntitySupplier;

    public ImportedISqlBuildContext(SSupplier<Object> sqlEntitySupplier) {
        AssertUtil.notNull(sqlEntitySupplier, "注册SQL执行环境必须配置有效的查询实体");
        this.sqlEntitySupplier = sqlEntitySupplier;
    }

    @Override
    protected Object provideSqlEntity() { return sqlEntitySupplier.get(); }
}
