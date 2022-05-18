package jmvc.model;

public abstract class Where implements Queryable {
    protected Where(String stmt, Select select) {
        _stmt = stmt;
        _select = select;
    }

    protected final String _stmt;
    protected final Select _select;
}
