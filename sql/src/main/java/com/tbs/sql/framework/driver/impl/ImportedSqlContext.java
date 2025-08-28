package com.tbs.sql.framework.driver.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.function.SSupplier;
import com.tbs.sql.framework.driver.AbstractSqlContext;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author abstergo
 */
public class ImportedSqlContext extends AbstractSqlContext {
	@Getter
	@Setter
	private SSupplier<Object> sqlEntitySupplier;

	public ImportedSqlContext(@NotNull SSupplier<Object> sqlEntitySupplier) {
		AssertUtil.notNull(sqlEntitySupplier, "注册SQL执行环境必须配置有效的查询实体");
		this.sqlEntitySupplier = sqlEntitySupplier;
	}

	@Override
	protected Object provideSqlEntity() {
		return sqlEntitySupplier.get();
	}
}
