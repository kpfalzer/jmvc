package jmvc.model.sql;

import gblibx.Util;
import jmvc.model.Select;
import jmvc.model.Table;
import jmvc.model.Where;

/**
 * SELECT from single table.
 */
public class SqlSelect extends Select {
    public SqlSelect(SqlTable table, String... cols) {
        super(table, cols);
    }

    public SqlSelect(SqlTable table, boolean distinct, String... cols) {
        super(table, distinct, cols);
    }

    @Override
    public Where where(String stmt) {
        return new SqlWhere(stmt, this);
    }

    @Override
    public String getStatement() {
        return String.format("SELECT %s %s FROM %s",
                _distinct ? "DISTINCT" : "",
                (0 == _cols.length) ? "*" : Util.join(_cols, ","),
                getTable().name
        );
    }

    @Override
    public Table getTable() {
        return _table;
    }
}
