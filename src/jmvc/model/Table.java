package jmvc.model;

import gblibx.Util;
import jmvc.Exception;

import java.sql.ResultSet;
import java.sql.SQLException;
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
        public static Util.Pair<String, ColInfo> create(ResultSet rs) {
            try {
                final String name = rs.getString("COLUMN_NAME");
                final ColInfo info = new ColInfo(rs);
                return new Util.Pair<>(name, info);
            } catch (SQLException e) {
                throw new Exception(e);
            }
        }
        public ColInfo(ResultSet rs) {
            try {
                type = rs.getInt("DATA_TYPE");
                size = rs.getInt("COLUMN_SIZE");
                position = rs.getInt("ORDINAL_POSITION");
            } catch (SQLException e) {
                throw new Exception(e);
            }
        }
        public final int type;
        public final int size;
        public final int position;
    }
}
