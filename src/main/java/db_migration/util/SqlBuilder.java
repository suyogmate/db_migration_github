package db_migration.util;

import java.sql.Types;
import java.util.List;

import db_migration.model.ColumnMeta;

public class SqlBuilder {

    public static String mapType(ColumnMeta c) {
        return switch (c.getJdbcType()) {
            case Types.INTEGER -> "INTEGER";
            case Types.BIGINT -> "BIGINT";
            case Types.VARCHAR -> "VARCHAR(" + c.getSize() + ")";
            case Types.TIMESTAMP -> "TIMESTAMP";
            case Types.DATE -> "DATE";
            case Types.NUMERIC -> "NUMERIC";
            default -> "TEXT";
        };
    }

    public static String createTable(String schema, String table, List<ColumnMeta> cols) {
        StringBuilder sql = new StringBuilder(
                "CREATE TABLE IF NOT EXISTS " + schema + "." + table + " ("
        );

        for (ColumnMeta c : cols) {
            sql.append(c.getName())
               .append(" ")
               .append(mapType(c))
               .append(c.isNullable() ? "" : " NOT NULL")
               .append(",");
        }
        sql.setLength(sql.length() - 1);
        sql.append(")");

        return sql.toString();
    }

    public static String insertSql(String schema, String table, int colCount) {
        return "INSERT INTO " + schema + "." + table +
                " VALUES (" + "?,".repeat(colCount).substring(0, colCount * 2 - 1) + ")";
    }
}
