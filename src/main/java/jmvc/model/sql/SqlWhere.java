package jmvc.model.sql;

import jmvc.model.Table;
import jmvc.model.Where;

public class SqlWhere extends Where {
    public SqlWhere(String stmt, SqlSelect select) {
        super(stmt, select);
    }

    @Override
    public String getStatement() {
        return String.format("%s WHERE %s",
                _select.getStatement(),
                _stmt
        );
    }

    @Override
    public Table getTable() {
        return _select.getTable();
    }
}
