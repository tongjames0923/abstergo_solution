package com.tbs.sql.builder;

import java.util.List;

import com.tbs.common.error.AssertUtil;
import com.tbs.sql.model.group.GroupModel;
import com.tbs.sql.model.limit.LimitModel;
import com.tbs.sql.model.order.OrderInfo;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.model.where.WhereCompose;
import org.jetbrains.annotations.NotNull;

/**
 * SQL驱动抽象类，提供SQL驱动接口的通用实现。
 *
 * <p>该类实现了 {@link SqlDriver} 接口，提供了SQL查询数据提取的模板方法。
 * 子类只需要实现各个具体的提取方法，而不需要关心数据验证和组装逻辑。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>提供SQL查询数据提取的模板方法 {@link #getSqlQueryData(SqlBuildContext)}</li>
 *   <li>验证上下文有效性</li>
 *   <li>组装SQL查询数据</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>作为SQL驱动的基类使用</li>
 *   <li>需要自定义SQL数据提取逻辑时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * public class CustomSqlDriver extends AbstractSqlDriver {
 *     @Override
 *     protected SelectInfoModel getSelectInfo(SqlBuildContext context) {
 *         // 实现SELECT信息提取
 *     }
 *     
 *     @Override
 *     protected WhereCompose getWhereInfo(SqlBuildContext context) {
 *         // 实现WHERE条件提取
 *     }
 *     // ... 其他方法实现
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>子类必须实现所有抽象方法</li>
 *   <li>上下文必须有效，否则会抛出异常</li>
 *   <li>可以通过 {@link #supportContext(SqlBuildContext)} 方法控制支持的上下文类型</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see SqlDriver
 * @see EntitySqlDriver
 */
public abstract class AbstractSqlDriver implements SqlDriver {
    protected abstract  SelectInfoModel getSelectInfo(SqlBuildContext context);
    protected abstract  WhereCompose getWhereInfo(SqlBuildContext context);
    protected abstract  List<GroupModel> getGroups(SqlBuildContext context);
    protected abstract  LimitModel getLimit(SqlBuildContext context);
    protected abstract  List<OrderInfo> getOrders(SqlBuildContext context);

    protected boolean supportContext(SqlBuildContext context) { return true; }

    @Override
    public SqlQueryData getSqlQueryData(SqlBuildContext context) {
        AssertUtil.notNull(context, "环境上下文不能为空");
        AssertUtil.isTrue(!supportContext(context), "不支持该环境上下文{0}", context);
        AssertUtil.isTrue(!context.isValid(), "环境上下文{0}已失效", context);
        return new SqlQueryData(context, getSelectInfo(context), getGroups(context), getLimit(context), getOrders(context), getWhereInfo(context));
    }
}
