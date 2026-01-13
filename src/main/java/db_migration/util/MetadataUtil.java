package db_migration.util;

import java.sql.*;
import java.util.*;

import db_migration.model.ColumnMeta;

public class MetadataUtil {

    public static List<String> getTables(Connection conn, String schema) throws Exception {
        List<String> tables = new ArrayList<>();
        ResultSet rs = conn.getMetaData()
                .getTables(null, schema, "%", new String[]{"TABLE"});
        while (rs.next()) {
            tables.add(rs.getString("TABLE_NAME"));
        }
        return tables;
    }

    public static List<ColumnMeta> getColumns(
            Connection conn, String schema, String table) throws Exception {

        List<ColumnMeta> cols = new ArrayList<>();
        ResultSet rs = conn.getMetaData()
                .getColumns(null, schema, table, "%");

        while (rs.next()) {
            ColumnMeta c = new ColumnMeta();
            c.setName(rs.getString("COLUMN_NAME"));
            c.setJdbcType(rs.getInt("DATA_TYPE"));
            c.setSize(rs.getInt("COLUMN_SIZE"));
            c.setNullable("YES".equals(rs.getString("IS_NULLABLE")));
            cols.add(c);
        }
        return cols;
    }
}
