package db_migration.util;

import java.sql.Types;
import java.util.Collections;
import java.util.List;

import db_migration.model.ColumnMeta;

public class SqlBuilder {

    /* ---------------- DATATYPE MAPPING ---------------- */

    public static String mapType(ColumnMeta c, String dbType) {

        if ("oracle".equalsIgnoreCase(dbType)) {
            return switch (c.getJdbcType()) {
                case Types.INTEGER -> "NUMBER(10)";
                case Types.BIGINT -> "NUMBER(19)";
                case Types.VARCHAR -> "VARCHAR2(" + Math.min(c.getSize(), 4000) + ")";
                case Types.DATE -> "DATE";
                case Types.TIMESTAMP -> "TIMESTAMP";
                case Types.NUMERIC -> "NUMBER";
                default -> "CLOB";
            };
        }

        // PostgreSQL
        return switch (c.getJdbcType()) {
            case Types.INTEGER -> "INTEGER";
            case Types.BIGINT -> "BIGINT";
            case Types.VARCHAR -> "VARCHAR(" + Math.min(c.getSize(), 65535) + ")";
            case Types.DATE -> "DATE";
            case Types.TIMESTAMP -> "TIMESTAMP";
            case Types.NUMERIC -> "NUMERIC";
            default -> "TEXT";
        };
    }

    /* ---------------- CREATE TABLE ---------------- */

    public static String createTable(
            String schema,
            String table,
            List<ColumnMeta> cols,
            String dbType
    ) {
        StringBuilder sql = new StringBuilder();

        if ("oracle".equalsIgnoreCase(dbType)) {
            sql.append("CREATE TABLE ")
               .append(schema).append(".").append(table).append(" (");
        } else {
            sql.append("CREATE TABLE IF NOT EXISTS ")
               .append(schema).append(".").append(table).append(" (");
        }

        for (ColumnMeta c : cols) {
            sql.append(c.getName().toUpperCase())
               .append(" ")
               .append(mapType(c, dbType))
               .append(c.isNullable() ? "" : " NOT NULL")
               .append(", ");
        }

        sql.setLength(sql.length() - 2);
        sql.append(")");

        return sql.toString();
    }

    /* ---------------- INSERT SQL ---------------- */

    public static String insertSql(String schema, String table, int colCount) {
        String placeholders = String.join(
                ",",
                Collections.nCopies(colCount, "?")
        );

        return "INSERT INTO " + schema + "." + table +
               " VALUES (" + placeholders + ")";
    }
}
