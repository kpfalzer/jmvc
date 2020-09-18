package jmvc.model;

import static gblibx.Util.isNonNull;
import static java.util.Objects.isNull;

public abstract class QueryResult {
    protected Object[][] _rows;
    protected Exception _exception;
    protected ColInfo[] _colInfos;

    public Exception exception() {
        return _exception;
    }

    public boolean hasException() {
        return isNonNull(exception());
    }

    public boolean isValid() {
        return !(hasException() || isNull(_rows));
    }

    public int nrows() {
        return (isValid()) ? _rows.length : 0;
    }

    public Object[][] rows() {
        return _rows;
    }

    public static class ColInfo {
        public ColInfo(String tableName, String colName, int colType) {
            this.colName = colName;
            this.colType = colType;
            this.tableName = tableName;
        }

        public final String colName, tableName;
        public final int colType;
    }
}
