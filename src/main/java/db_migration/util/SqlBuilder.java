package db_migration.util;

import java.sql.Types;
import java.util.List;
import java.util.stream.Collectors;

import db_migration.model.ColumnMeta;

public class SqlBuilder {

    public static String createTable(String dbType,
                                     String schema,
                                     String table,
                                     List<ColumnMeta> cols) {

        String columns = cols.stream()
            .map(c -> c.getName() + " " + mapType(dbType, c))
            .collect(Collectors.joining(", "));

        return "CREATE TABLE " + schema + "." + table + " (" + columns + ")";
    }

    public static String insertSql(String schema,
                                   String table,
                                   List<ColumnMeta> cols) {

        String placeholders = cols.stream()
                .map(c -> "?")
                .collect(Collectors.joining(","));

        return "INSERT INTO " + schema + "." + table +
                " VALUES (" + placeholders + ")";
    }

    private static String mapType(String dbType, ColumnMeta c) {

        if ("oracle".equalsIgnoreCase(dbType)) {
            return switch (c.getJdbcType()) {
                case Types.INTEGER, Types.BIGINT -> "NUMBER";
                case Types.VARCHAR -> "VARCHAR2(" + c.getSize() + ")";
                case Types.TIMESTAMP -> "TIMESTAMP";
                case Types.DATE -> "DATE";
                case Types.NUMERIC -> "NUMBER";
                default -> "CLOB";
            };
        }

        // PostgreSQL
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
}
