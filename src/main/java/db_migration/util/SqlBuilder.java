package db_migration.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import db_migration.model.DatabaseConfig;

public class SqlBuilder {

    /* ================= CREATE TABLE ================= */

    public static String buildCreateTable(
            Connection src,
            Connection tgt,
            String table,
            DatabaseConfig srcCfg,
            DatabaseConfig tgtCfg
    ) throws Exception {

        DatabaseMetaData meta = src.getMetaData();
        ResultSet rs = meta.getColumns(null, srcCfg.getSchema(), table, null);

        List<String> cols = new ArrayList<>();

        while (rs.next()) {
            String name = rs.getString("COLUMN_NAME");
            int type = rs.getInt("DATA_TYPE");
            int size = rs.getInt("COLUMN_SIZE");
            int scale = rs.getInt("DECIMAL_DIGITS");

            cols.add(name + " " + mapType(type, size, scale, tgtCfg.getDbType()));
        }

        String ddl;

        if (isOracle(tgtCfg)) {
            ddl = "CREATE TABLE " + tgtCfg.getSchema() + "." + table +
                    " (" + String.join(",", cols) + ")";
        } else {
            ddl = "CREATE TABLE IF NOT EXISTS " + tgtCfg.getSchema() + "." + table +
                    " (" + String.join(",", cols) + ")";
        }

        return ddl;
    }

    /* ================= TYPE MAPPING ================= */

    private static String mapType(int jdbcType, int size, int scale, String targetDb) {

        if ("oracle".equalsIgnoreCase(targetDb)) {
            return switch (jdbcType) {
                case Types.INTEGER, Types.BIGINT -> "NUMBER";
                case Types.NUMERIC -> "NUMBER(" + size + "," + scale + ")";
                case Types.VARCHAR -> "VARCHAR2(" + size + ")";
                case Types.CLOB -> "CLOB";
                case Types.DATE, Types.TIMESTAMP -> "DATE";
                default -> "CLOB";
            };
        }

        // PostgreSQL
        return switch (jdbcType) {
            case Types.INTEGER -> "INTEGER";
            case Types.BIGINT -> "BIGINT";
            case Types.NUMERIC -> "NUMERIC(" + size + "," + scale + ")";
            case Types.VARCHAR -> "VARCHAR(" + size + ")";
            case Types.TIMESTAMP -> "TIMESTAMP";
            case Types.DATE -> "DATE";
            default -> "TEXT";
        };
    }

    private static boolean isOracle(DatabaseConfig cfg) {
        return "oracle".equalsIgnoreCase(cfg.getDbType());
    }
}
