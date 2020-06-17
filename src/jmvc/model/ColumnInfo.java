package jmvc.model;

import java.util.HashMap;
import java.util.Map;

import static gblibx.Util.isNonNull;

public class ColumnInfo {
    public ColumnInfo(
            Database dbase, Table table,
            String name,
            int type, int size, int position, String defaultVal, boolean isPrimaryKey) {
        this.dbase = dbase;
        this.table = table;
        this.name = name;
        this.type = type;
        this.size = size;
        this.position = position;
        this.defaultVal = defaultVal;
        this.isPrimaryKey = isPrimaryKey;
        add();
    }

    public boolean hasDefaultVal() {
        return isNonNull(defaultVal);
    }

    public final int type;
    public final int size;
    public final int position;
    public final String defaultVal;
    public final boolean isPrimaryKey;
    public final Database dbase;
    public final Table table;
    public final String name;
    /**
     * Unique id across all columns, tables, databases.
     */
    public final int id = _colInfoById.size();

    /**
     * Fully qualified name: DATABASE.TABLE.COLUMN.
     * @return fully qualified name.
     */
    public String getFQN() {
        return String.format("%s.%s.%s", dbase.name(), table.name, name).toUpperCase();
    }

    private void add() {
        _colInfoById.put(id, this);
        _colInfoByName.put(getFQN(), this);
    }

    private static final Map<Integer, ColumnInfo> _colInfoById = new HashMap<>();
    private static final Map<String, ColumnInfo> _colInfoByName = new HashMap<>();
}
