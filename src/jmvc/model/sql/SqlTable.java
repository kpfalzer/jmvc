package jmvc.model.sql;

import gblibx.Util;
import jmvc.Exception;
import jmvc.model.Database;
import jmvc.model.Table;

import java.sql.ResultSet;
import java.sql.SQLException;

import static gblibx.Util.upcase;

public class SqlTable extends Table {
    public static SqlTable create(Database dbase, String name, String... eles) {
        return new SqlTable(name, _getConfig(eles), dbase);
    }

    private SqlTable(String name, Config config, Database dbase) {
        super(name, config, dbase);
    }

    @Override
    protected void _getColumnInfo() {
        try {
            ResultSet rs = _dbase.getConnection()
                        .getMetaData()
                        .getColumns(null, null, upcase(name), null);
            while (rs.next()) {
                Util.Pair<String, ColInfo> info = ColInfo.create(rs);
                _colInfoByColName.put(info.v1, info.v2);
            }
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
