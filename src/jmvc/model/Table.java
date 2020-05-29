package jmvc.model;

import jmvc.Exception;

import static gblibx.Util.castobj;

public abstract class Table {

    protected Table(String name, Enum[] config, Database dbase) {
        this.name = name;
        this._config = config;
        this._dbase = dbase;
    }

    /**
     * Column config (enum) must implement.
     */
    public interface ColSpec {
        public String getSpec();
    }

    protected String[] _getColumnSpec(Enum e) {
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
     * @param colName column name.
     * @return ordinal value.
     */
    protected int _getColInfoIx(String colName) {
        for (Enum e : _config) {
            if (e.name().equalsIgnoreCase(colName))
                return e.ordinal();
        }
        throw new Exception.TODO("invalid column: "+colName);
    }

    protected abstract void _setColumnInfo();

    protected abstract void _createTable();

    public final String name;
    protected final Enum[] _config;
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

        public final int type;
        public final int size;
        public final int position;
        public final String defaultVal;
        public final boolean isPrimaryKey;
    }
}
