package jmvc.model;

import jmvc.Exception;

import java.util.EnumSet;

import static gblibx.Util.*;

public abstract class Table<E extends Enum<E>> {

    protected Table(String name, Class<E> config, Database dbase) {
        this.name = name;
        _configSet = EnumSet.allOf(config);
        _config = __universe();
        _dbase = dbase;
        _colEnumCls = config;
    }

    protected Enum<E> _getEnumOfCol(String col) {
        return Enum.<E>valueOf(_colEnumCls, col.toUpperCase());
    }

    /**
     * All Enum values (columns).
     *
     * @return all Enum values.
     */
    private Enum<E>[] __universe() {
        Enum<E>[] u = new Enum[_configSet.size()];
        for (Enum<E> e : _configSet) {
            u[e.ordinal()] = e;
        }
        return u;
    }

    /**
     * Column config (enum) must implement.
     */
    public interface ColSpec {
        public String getSpec();
    }

    protected String[] _getColumnSpec(Enum<E> e) {
        final ColSpec specObj = castobj(e);
        return specObj.getSpec().replace("?", e.name()).split("\\s*;\\s*");
    }

    public void initialize() {
        boolean hasTable = _dbase.hasTable(name);
        if (!hasTable) {
            _createTable();
        }
        _setColumnInfo();
    }

    /**
     * Return ordinal of colName.
     *
     * @param colName column name.
     * @return ordinal value.
     */
    protected int _getColInfoOrdinal(String colName, boolean throwOnFail) {
        for (Enum<E> e : _config) {
            if (e.name().equalsIgnoreCase(colName))
                return e.ordinal();
        }
        if (throwOnFail)
            throw new Exception.TODO("invalid column: " + colName);
        else
            return -1;
    }

    protected int _getColInfoOrdinal(String colName) {
        return _getColInfoOrdinal(colName, true);
    }

    protected boolean _hasColumn(String colName) {
        return 0 <= _getColInfoOrdinal(colName, false);
    }

    protected abstract void _setColumnInfo();

    protected abstract void _createTable();

    protected boolean _isValidColumn(Enum<E> col) {
        return _configSet.contains(col);
    }

    /**
     * Insert a new row into table.
     *
     * @param colVals pairs of Enum/col values.
     * @return generated ID or -1 if no ID.
     */
    public abstract int insertRow(Object... colVals);

    /**
     * Common variety UPDATE TABLE SET col1=val1... WHERE ID=id.
     *
     * @param id      ID value.
     * @param colVals pairs of Enum/col values.
     * @return updated ID or -1 if no update done.
     */
    public abstract int updateTableById(int id, Object... colVals);

    public final String name;
    protected final Enum<E>[] _config;
    protected final EnumSet<E> _configSet;
    protected final Database _dbase;
    protected final Class<E> _colEnumCls;
    /**
     * ColInfo by _config ordinal.
     */
    protected ColInfo[] _colInfo;

    public static class ColInfo {
        public ColInfo(int type, int size, int position, String defaultVal, boolean isPrimaryKey) {
            this.type = type;
            this.size = size;
            this.position = position;
            this.defaultVal = defaultVal;
            this.isPrimaryKey = isPrimaryKey;
        }

        public boolean hasDefaultVal() {
            return isNonNull(defaultVal);
        }

        public final int type;
        public final int size;
        public final int position;
        public final String defaultVal;
        public final boolean isPrimaryKey;
    }
}
