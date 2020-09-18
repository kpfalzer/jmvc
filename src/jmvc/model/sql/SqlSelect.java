package jmvc.model.sql;

import gblibx.Util;
import jmvc.model.QueryResult;
import jmvc.model.Select;
import jmvc.model.Table;
import jmvc.model.Where;

import static java.util.Objects.isNull;

/**
 * SELECT from single table.
 */
public class SqlSelect extends Select {
    public SqlSelect(SqlTable table, String... cols) {
        super(table, cols);
    }

    @Override
    public Where where(String stmt) {
        return new SqlWhere(stmt, this);
    }

    @Override
    public String getStatement() {
        return String.format("SELECT %s FROM %s",
                isNull(_cols) ? "*" : Util.join(_cols, ","),
                getTable().name
        );
    }

    @Override
    public QueryResult execute() {
        return getTable().executeQuery(getStatement());
    }

    @Override
    public Table getTable() {
        return _table;
    }
}
