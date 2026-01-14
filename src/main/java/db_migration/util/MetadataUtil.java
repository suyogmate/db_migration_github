package db_migration.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import db_migration.model.ColumnMeta;

public class MetadataUtil {

    /* -------------------------------------------------
     * GET TABLE LIST
     * ------------------------------------------------- */
    public static List<String> getTables(Connection conn, String schema) throws Exception {

        List<String> tables = new ArrayList<>();
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet rs = meta.getTables(
                null,
                schema,
                "%",
                new String[]{"TABLE"})) {

            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        return tables;
    }

    /* -------------------------------------------------
     * CHECK IF TABLE EXISTS  ✅ FIX FOR YOUR ERROR
     * ------------------------------------------------- */
    public static boolean tableExists(Connection conn,
                                      String schema,
                                      String table) throws Exception {

        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet rs = meta.getTables(
                null,
                schema,
                table,
                new String[]{"TABLE"})) {

            return rs.next();
        }
    }

    /* -------------------------------------------------
     * GET COLUMN METADATA
     * ------------------------------------------------- */
    public static List<ColumnMeta> getColumns(Connection conn,
                                              String schema,
                                              String table) throws Exception {

        List<ColumnMeta> cols = new ArrayList<>();
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet rs = meta.getColumns(
                null,
                schema,
                table,
                "%")) {

            while (rs.next()) {
                ColumnMeta c = new ColumnMeta();
                c.setName(rs.getString("COLUMN_NAME"));
                c.setJdbcType(rs.getInt("DATA_TYPE"));
                c.setSize(rs.getInt("COLUMN_SIZE"));
                c.setNullable(
                        rs.getInt("NULLABLE")
                                == DatabaseMetaData.columnNullable
                );
                cols.add(c);
            }
        }
        return cols;
    }
}
