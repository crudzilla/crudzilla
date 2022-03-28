package io.github.crudzilla.persistency.sqlserver;

import org.hibernate.dialect.pagination.AbstractLimitHandler;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitHelper;
import org.hibernate.engine.spi.RowSelection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLServerLimitHandler extends AbstractLimitHandler implements LimitHandler {

	@Override
	public boolean supportsLimit() { return true; }

	@Override
	public boolean supportsLimitOffset() { return true; }

	@Override
	public boolean supportsVariableLimit() { return true; }

	@Override
	public boolean useMaxForLimit() { return true; }

	@Override
	public String processSql(String sql, RowSelection selection) {
		StringBuilder sb = new StringBuilder(sql);

		if (LimitHelper.hasMaxRows(selection)) {
			if (!sql.contains(" order by ")) {
				sb.append(" order by 1 asc ");
			}

			sb.append(" offset ? rows fetch next ? rows only ");
		}

		return sb.toString();
	}

	@Override
	public int bindLimitParametersAtStartOfQuery(RowSelection selection, PreparedStatement statement, int index) throws SQLException {
		return 0;
	}

	@Override
	public int bindLimitParametersAtEndOfQuery(RowSelection selection, PreparedStatement statement, int index) throws SQLException {
        if (LimitHelper.hasMaxRows(selection)) {

            if (selection.getFirstRow() == null) {
                selection.setFirstRow(0);
            }

            statement.setInt(index, selection.getFirstRow());
            statement.setInt(index + 1, selection.getMaxRows());
            return 2;
        }

        return 0;
	}
}