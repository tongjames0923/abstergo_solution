package com.tbs.sql.framework.extractor.impls.mysql;

import java.util.List;

import com.tbs.common.util.CollUtil;
import com.tbs.sql.framework.extractor.IGroupSqlExtractor;
import com.tbs.sql.framework.model.SqlGroupModel;

/**
 * mysql方言的分组sql提取器
 *
 * @author abstergo
 */
public class MysqlGroupExtractor implements IGroupSqlExtractor {

	@Override
	public String extraGroupBySql(List<SqlGroupModel> sqlGroupModels) {
		if (CollUtil.isEmpty(sqlGroupModels)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (SqlGroupModel sqlGroupModel : sqlGroupModels) {
			sb.append(sqlGroupModel.getFieldName());
			sb.append(",");
		}
		if (sb.length() == 0) {
			return "";
		}
		return " group by " + sb.substring(0, sb.length() - 1);
	}
}
