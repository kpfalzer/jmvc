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
    public static SqlTable create(Database dbase, String name, Enum[] cols) {
        return new SqlTable(name, cols, dbase);
    }

    private SqlTable(String name, Enum[] cols, Database dbase) {
        super(name, cols, dbase);
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
            throw new Exception.TODO(e);
        }
    }

    @Override
    protected void _setColumnInfo() {
        _colInfo = new ColInfo[_config.length];
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
                _colInfo[_getColInfoIx(info.v1)] = info.v2;
            }
        } catch (SQLException e) {
            throw new Exception.TODO(e);
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
            throw new Exception.TODO(e);
        }
    }

    @Override
    protected void _createTable() {
        StringBuilder xstmt = new StringBuilder(
                String.format("CREATE TABLE %s (", name));
        int i = 0;
        for (Enum colSpec : super._config) {
            for (String c : _getColumnSpec(colSpec))
                xstmt
                        .append((0 < i++) ? "," : "")
                        .append("\n")
                        .append(c);
        }
        xstmt.append(")");
        try {
            _dbase.executeStatement(xstmt.toString());
        } catch (Exception ex) {
            throw new Exception.TODO(ex);
        }
    }
}
