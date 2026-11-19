package com.tbs.sql.dialect.extractor;

import com.tbs.common.util.base.CollUtil;
import com.tbs.sql.dialect.IGroupExtractor;
import com.tbs.sql.model.group.GroupModel;
import com.tbs.sql.util.SqlIdentifierUtils;

import java.util.List;

/**
 * 通用GROUP BY语句提取器，适用于MySQL、PostgreSQL、SQL Server、Oracle等数据库。
 *
 * <p>所有主流关系型数据库的GROUP BY语法完全一致，
 * 因此使用单一公共实现，避免各方言重复定义。</p>
 *
 * <p>实现说明：与 {@link CommonOrderExtractor} 一致，一次成型避免中间串。</p>
 *
 * @author abstergo
 * @since 2.0.0
 * @see IGroupExtractor
 */
public class CommonGroupExtractor implements IGroupExtractor {

    @Override
    public String extract(List<GroupModel> groupModels) {
        if (CollUtil.isEmpty(groupModels)) return "";
        StringBuilder sb = new StringBuilder(10 + groupModels.size() * 16).append(" GROUP BY ");
        for (GroupModel model : groupModels) {
            sb.append(SqlIdentifierUtils.column(model.getFieldName())).append(",");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
