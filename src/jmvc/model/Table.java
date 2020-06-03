package jmvc.model;

import jmvc.Exception;

import java.util.EnumSet;

import static gblibx.Util.castobj;
import static gblibx.Util.isNonNull;

public abstract class Table <E extends Enum<E>> {

    protected Table(String name, Class<E> config, Database dbase) {
        this.name = name;
        this._configSet = EnumSet.allOf(config);
        this._config = __universe();
        this._dbase = dbase;
    }

    private Enum<E>[] __universe() {
        Enum<E> u[] = new Enum[_configSet.size()];
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
    protected int _getColInfoIx(String colName) {
        for (Enum<E> e : _config) {
            if (e.name().equalsIgnoreCase(colName))
                return e.ordinal();
        }
        throw new Exception.TODO("invalid column: " + colName);
    }

    protected abstract void _setColumnInfo();

    protected abstract void _createTable();

    protected boolean _isValidColumn(Enum<E> col) {
        return _configSet.contains(col);
    }

    /**
     * Insert a new row into table.
     *
     * @param colVals pairs of Enum/col, value
     * @return generated ID or -1 if no ID.
     */
    public abstract int insertRow(Object... colVals);

    public final String name;
    protected final Enum<E>[] _config;
    protected final EnumSet<E> _configSet;
    protected final Database _dbase;
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
