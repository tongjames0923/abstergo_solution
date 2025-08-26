package com.tbs.sql.framework.driver;

import java.util.List;

import com.tbs.sql.framework.model.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface IQueryDriver {
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static final class SqlQueryData {
		private ISqlContext context;
		private SqlSelectInfoModel selectInfo;
		private List<SqlGroupModel> sqlGroups;
		private SqlLimitModel sqlLimit;
		private List<SqlOrderInfo> sqlOrders;
		private SqlWhereCompose sqlWhereInfo;
	}

	public SqlQueryData getSqlQueryData(ISqlContext context);

}
