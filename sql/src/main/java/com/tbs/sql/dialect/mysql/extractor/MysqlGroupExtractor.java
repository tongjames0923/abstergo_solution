package com.tbs.sql.dialect.mysql.extractor;

import com.tbs.common.util.base.CollUtil;
import com.tbs.sql.dialect.IGroupExtractor;
import com.tbs.sql.model.group.GroupModel;

import java.util.List;

public class MysqlGroupExtractor implements IGroupExtractor {
    @Override
    public String extract(List<GroupModel> groupModels) {
        if (CollUtil.isEmpty(groupModels)) return "";
        StringBuilder sb = new StringBuilder();
        for (GroupModel model : groupModels) {
            sb.append(model.getFieldName()).append(",");
        }
        if (sb.length() == 0) return "";
        return " group by " + sb.substring(0, sb.length() - 1);
    }
}
