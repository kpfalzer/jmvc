package jmvc.model;

import jmvc.JmvcException;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static gblibx.Util.castobj;
import static gblibx.Util.stream;

public abstract class Table<E extends Enum<E>> {

    protected Table(String name, Class<E> config, Database dbase) {
        this.name = name;
        _configSet = EnumSet.allOf(config);
        _config = universe();
        _dbase = dbase;
        _colEnumCls = config;
    }

    protected Enum<E> getEnumOfCol(String col) {
        return Enum.<E>valueOf(_colEnumCls, col.toUpperCase());
    }

    /**
     * All Enum values (columns).
     *
     * @return all Enum values.
     */
    private Enum<E>[] universe() {
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

    protected String[] getColumnSpec(Enum<E> e) {
        final ColSpec specObj = castobj(e);
        return specObj.getSpec().replace("?", e.name()).split("\\s*;\\s*");
    }

    public void initialize() {
        boolean hasTable = _dbase.hasTable(name);
        if (!hasTable) {
            createTable();
        }
        setColumnInfo();
    }

    /**
     * Return ordinal of colName.
     *
     * @param colName column name.
     * @return ordinal value.
     */
    protected int getColInfoOrdinal(String colName, boolean throwOnFail) {
        for (Enum<E> e : _config) {
            if (e.name().equalsIgnoreCase(colName))
                return e.ordinal();
        }
        if (throwOnFail)
            throw new JmvcException.TODO("invalid column: " + colName);
        else
            return -1;
    }

    protected int getColInfoOrdinal(String colName) {
        return getColInfoOrdinal(colName, true);
    }

    protected boolean hasColumn(String colName) {
        return 0 <= getColInfoOrdinal(colName, false);
    }

    protected abstract void setColumnInfo();

    protected abstract void createTable();

    protected boolean isValidColumn(Enum<E> col) {
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
     * Select columns from Table.
     * @param cols columns to select.
     * @return Select object.
     */
    public abstract Select select(String... cols);

    public abstract Select selectDistinct(String... cols);

    public int insertRow(Map<String, Object> kvs) {
        Object colVals[] = new Object[2 * kvs.size()];
        int i = 0;
        for (Map.Entry<String,Object> kv : kvs.entrySet()) {
            try {
                colVals[i++] = getEnumOfCol(kv.getKey());
                colVals[i++] = kv.getValue();
            } catch (IllegalArgumentException ex) {
                throw new JmvcException.TODO(ex);
            }
        }
        return insertRow(colVals);
    }

    /**
     * CREATE INDEX on designated columns.
     * @param cols column names.
     */
    public abstract void createIndex(String... cols);

    /**
     * Common variety UPDATE TABLE SET col1=val1... WHERE ID=id.
     *
     * @param id      ID value.
     * @param colVals pairs of Enum/col values.
     * @return updated ID or -1 if no update done.
     */
    public abstract int updateTableById(int id, Object... colVals);

    /**
     * Find rows in table by id.
     * @param ids one or more id.
     * @param orderByIdAsc true to order by ID ascending; else order by ID descending.
     * @return sorted rows.
     */
    public abstract Object findById(Stream<Integer> ids, boolean orderByIdAsc);

    public final Object findById(Integer id, boolean orderByIdAsc) {
        return findById(stream(id), orderByIdAsc);
    }

    public final Object findById(Stream<Integer> ids) {
        return findById(ids, true);
    }

    public final Object findById(Integer id) {
        return findById(stream(id), true);
    }

    public abstract QueryResult executeQuery(String statement);

    /**
     * Raw query handler.
     * @param statement DBMS statement to execute.
     * @param forEachRow Process each (raw) row and return true on early terminate/close.
     * @return result (not expected to be useful, since raw handler likely squirelled results local).
     */
    public abstract QueryResult executeQuery(String statement, Function<Object[], Boolean> forEachRow);

    /**
     * Select col value from cols.
     * @param cols columns from "select *"
     * @param col column field
     * @return column value
     */
    public Object getVal(Object[] cols, Enum<E> col) {
        return cols[col.ordinal()];
    }

    public static <E extends Enum<E>,T> T valueOf(Object[] cols, Enum<E> col) {
        return castobj(cols[col.ordinal()]);
    }

    public final String name;
    protected final Enum<E>[] _config;
    protected final EnumSet<E> _configSet;
    protected final Database _dbase;
    protected final Class<E> _colEnumCls;
    /**
     * ColInfo by _config ordinal.
     */
    protected ColumnInfo[] _colInfo;

}
