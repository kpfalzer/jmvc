package jmvc.model;

public abstract class Select implements Queryable {
    protected Select(Table table, String... cols) {
        _table = table;
        _cols = cols;
    }

    protected final String[] _cols;
    protected final Table _table;

    public abstract Where where(String stmt);

    public Where whereEQ(String lhs, String rhs) {
        return where(String.format("%s = %s", lhs, rhs));
    }
}
