package jmvc.model;

import static gblibx.Util.isNonNull;

public interface Queryable {
    public String getStatement();

    public default QueryResult execute(String moreQuery) {
        StringBuilder stmt = new StringBuilder(getStatement());
        if (isNonNull(moreQuery)) {
            stmt.append(" ").append(moreQuery);
        }
        return getTable().executeQuery(stmt.toString());
    }

    public default QueryResult execute() {
        return execute(null);
    }

    public Table getTable();
}
