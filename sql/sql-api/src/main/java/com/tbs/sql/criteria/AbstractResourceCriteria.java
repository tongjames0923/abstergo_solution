package com.tbs.sql.criteria;

import cn.hutool.core.io.resource.ResourceUtil;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.sql.model.select.SelectInfoModel;

import java.io.BufferedReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象资源查询条件类，从资源文件中加载查询SQL。
 *
 * <p>该类实现了 {@link IQueryCriteria} 接口，提供了从资源文件（如classpath下的SQL文件）
 * 加载查询SQL的功能。支持缓存机制，避免重复加载相同的资源。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>从资源文件加载查询SQL</li>
 *   <li>缓存已加载的SQL，避免重复加载</li>
 *   <li>支持UTF-8编码的资源文件</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要从外部SQL文件加载查询语句时</li>
 *   <li>需要管理复杂的SQL语句时</li>
 *   <li>需要复用SQL查询语句时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * public class UserCriteria extends AbstractResourceCriteria {
 *     public UserCriteria() {
 *         putResourcePath("classpath:sql/user_query.sql");
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>资源文件必须是UTF-8编码</li>
 *   <li>资源路径必须正确，否则会抛出异常</li>
 *   <li>缓存是静态的，所有实例共享</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see IQueryCriteria
 */
public abstract class AbstractResourceCriteria implements IQueryCriteria {

    private String resourcePath;
    private static Map<String, SelectInfoModel> sqlSelectInfoModelMap = new ConcurrentHashMap<>(32);

    protected SelectInfoModel loadSql(String resourcePath) {
        try (BufferedReader reader = ResourceUtil.getUtf8Reader(resourcePath)) {
            final StringBuilder sql = new StringBuilder();
            reader.lines().forEach(sql::append);
            return SelectInfoModel.rawSql(sql.toString().trim());
        } catch (Exception e) {
            FormattedRuntimeException error = new FormattedRuntimeException(
                I18nUtil.get("sql.resource.load.failed", resourcePath), e);
            AssertUtil.throwError(() -> error);
        }
        FormattedRuntimeException error =
            new FormattedRuntimeException(I18nUtil.get("sql.resource.load.failed", resourcePath));
        AssertUtil.throwError(() -> error);
        throw error;
    }

    protected void putResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    protected String resourcePath() {
        return resourcePath;
    }

    @Override
    public SelectInfoModel getSelectSQLInfo() {
        return sqlSelectInfoModelMap.computeIfAbsent(resourcePath(), this::loadSql);
    }
}
