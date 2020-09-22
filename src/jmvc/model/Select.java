package jmvc.model;

public abstract class Select implements Queryable {
    protected Select(Table table, String... cols) {
        this(table, false, cols);
    }

    protected Select(Table table, boolean distinct, String... cols) {
        _table = table;
        _cols = cols;
        _distinct = distinct;
    }

    protected final String[] _cols;
    protected final Table _table;
    protected final boolean _distinct;

    public abstract Where where(String stmt);

    public Where whereEQ(String lhs, String rhs) {
        return where(String.format("%s = %s", lhs, rhs));
    }
}
