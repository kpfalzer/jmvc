package jmvc.model;

public interface Queryable {
    public String getStatement();

    public QueryResult execute();

    public Table getTable();
}
