package jmvc.model.sql;

import gblibx.Util;
import jmvc.Exception;
import jmvc.model.Database;
import jmvc.model.Table;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static gblibx.Util.upcase;

public class SqlTable extends Table {
    public static SqlTable create(Database dbase, String name, String... eles) {
        return new SqlTable(name, _getConfig(eles), dbase);
    }

    private SqlTable(String name, Config config, Database dbase) {
        super(name, config, dbase);
    }

    private SqlDatabase __dbase() {
        return SqlDatabase.myDbase(_dbase);
    }

    private DatabaseMetaData __getMetaData() {
        try {
            return __dbase()
                    .getMyConnection()
                    .getMetaData();
        } catch (SQLException e) {
            throw new Exception(e);
        }
    }

    @Override
    protected void _getColumnInfo() {
        final String tblName = upcase(name);
        Set<String> primaryKeys = new HashSet<>();
        try {
            ResultSet rs = __getMetaData()
                    .getPrimaryKeys(null, null, tblName);
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
            rs = __getMetaData()
                    .getColumns(null, null, tblName, null);
            while (rs.next()) {
                Util.Pair<String, ColInfo> info = __create(rs, primaryKeys);
                _colInfoByColName.put(info.v1, info.v2);
            }
        } catch (SQLException e) {
            throw new Exception(e);
        }
    }

    private static Util.Pair<String, ColInfo> __create(ResultSet rs, Set<String> pk) {
        try {
            final String name = rs.getString("COLUMN_NAME");
            final ColInfo info = new ColInfo(
                    rs.getInt("DATA_TYPE"),
                    rs.getInt("COLUMN_SIZE"),
                    rs.getInt("ORDINAL_POSITION"),
                    rs.getString("COLUMN_DEF"),
                    pk.contains(name)
            );
            return new Util.Pair<>(name, info);
        } catch (SQLException e) {
            throw new Exception(e);
        }
    }

    @Override
    protected void _createTable() {
        StringBuilder xstmt = new StringBuilder(
                String.format("CREATE TABLE %s (", name));
        int i = 0;
        for (String col : super._config) {
            xstmt
                    .append((0 < i++) ? "," : "")
                    .append("\n")
                    .append(col);
        }
        xstmt.append(")");
        try {
            _dbase.executeStatement(xstmt.toString());
        } catch (Exception ex) {
            boolean debug = true;
        }
    }
}
