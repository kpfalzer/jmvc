package jmvc.model;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public abstract class Table {

    protected Table(String name, Config config, Database dbase) {
        this.name = name;
        this._config = config;
        this._dbase = dbase;
    }

    /**
     * Create configuration.
     *
     * @param eles series of clauses to CREATE TABLE
     * @return list of clauses.
     */
    protected static Config _getConfig(String... eles) {
        return new Config(eles);
    }

    public static class Config extends LinkedList<String> {
        private Config(String[] eles) {
            for (String s : eles) super.add(s);
        }
    }

    public void initialize() {
        boolean hasTable = _dbase.hasTable(name);
        if (!hasTable) {
            _createTable();
        }
        _getColumnInfo();
    }

    protected abstract void _getColumnInfo();

    protected abstract void _createTable();

    public final String name;
    protected final Config _config;
    protected final Database _dbase;
    protected LinkedHashMap<String, ColInfo> _colInfoByColName = new LinkedHashMap<>();

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
