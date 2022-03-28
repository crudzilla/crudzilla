package io.github.crudzilla.persistency.sqlserver;

import org.hibernate.dialect.SQLServer2012Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.type.StandardBasicTypes;

public class SQLServerDialect extends SQLServer2012Dialect {

	@Override
	public LimitHandler getLimitHandler() { return new SQLServerLimitHandler(); }

	public SQLServerDialect() {
		super();
		this.registerFunction("dayofweek", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "datepart(weekday, ?1)"));
	}
}