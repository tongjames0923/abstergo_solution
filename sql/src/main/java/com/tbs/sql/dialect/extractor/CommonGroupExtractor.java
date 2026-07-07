package com.tbs.sql.dialect.extractor;

import com.tbs.common.util.base.CollUtil;
import com.tbs.sql.dialect.IGroupExtractor;
import com.tbs.sql.model.group.GroupModel;

import java.util.List;

/**
 * 通用GROUP BY语句提取器，适用于MySQL、PostgreSQL、SQL Server、Oracle等数据库。
 *
 * <p>所有主流关系型数据库的GROUP BY语法完全一致，
 * 因此使用单一公共实现，避免各方言重复定义。</p>
 *
 * @author abstergo
 * @since 2.0.0
 * @see IGroupExtractor
 */
public class CommonGroupExtractor implements IGroupExtractor {

    @Override
    public String extract(List<GroupModel> groupModels) {
        if (CollUtil.isEmpty(groupModels)) return "";
        StringBuilder sb = new StringBuilder();
        for (GroupModel model : groupModels) {
            sb.append(model.getFieldName()).append(",");
        }
        if (sb.length() == 0) return "";
        return " GROUP BY " + sb.substring(0, sb.length() - 1);
    }
}
