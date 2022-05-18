package jmvc.model;

import static gblibx.Util.invariant;
import static gblibx.Util.isEven;

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
        return _whereEQ(lhs, rhs);
    }

    public Where whereEQ(String... lhsRhs) {
        return _whereEQ(lhsRhs);
    }

    private Where _whereEQ(String... lhsRhs) {
        invariant(isEven(lhsRhs.length));
        StringBuilder q = new StringBuilder();
        for (int i = 0; i < lhsRhs.length; i += 2) {
            if (0 < q.length()) q.append(" AND ");
            q.append(lhsRhs[i]).append(" = ").append(lhsRhs[i+1]);
        }
        return where(q.toString());
    }
}
