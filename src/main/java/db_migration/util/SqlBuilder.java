package db_migration.util;

import java.sql.Types;
import java.util.List;

import db_migration.model.ColumnMeta;

public class SqlBuilder {

    /**
     * Maps JDBC types to TARGET database SQL types
     */
    public static String mapType(ColumnMeta c, String targetDbType) {

        if ("oracle".equalsIgnoreCase(targetDbType)) {
            return mapOracleType(c);
        }
        return mapPostgresType(c);
    }

    private static String mapPostgresType(ColumnMeta c) {
        return switch (c.getJdbcType()) {
            case Types.INTEGER -> "INTEGER";
            case Types.BIGINT -> "BIGINT";
            case Types.VARCHAR, Types.CHAR ->
                    "VARCHAR(" + Math.min(c.getSize(), 10485760) + ")";
            case Types.TIMESTAMP -> "TIMESTAMP";
            case Types.DATE -> "DATE";
            case Types.NUMERIC, Types.DECIMAL ->
                    "NUMERIC(" + c.getSize() + "," + c.getScale() + ")";
            case Types.CLOB, Types.LONGVARCHAR -> "TEXT";
            default -> "TEXT";
        };
    }

    private static String mapOracleType(ColumnMeta c) {
        return switch (c.getJdbcType()) {
            case Types.INTEGER, Types.BIGINT ->
                    "NUMBER";
            case Types.VARCHAR, Types.CHAR ->
                    "VARCHAR2(" + Math.min(c.getSize(), 4000) + ")";
            case Types.TIMESTAMP ->
                    "TIMESTAMP";
            case Types.DATE ->
                    "DATE";
            case Types.NUMERIC, Types.DECIMAL ->
                    "NUMBER(" + c.getSize() + "," + c.getScale() + ")";
            case Types.CLOB, Types.LONGVARCHAR ->
                    "CLOB";
            default ->
                    "CLOB";
        };
    }

    /**
     * CREATE TABLE builder
     */
    public static String createTable(String schema,
                                     String table,
                                     List<ColumnMeta> cols,
                                     String targetDbType) {

        String fullName = (schema == null || schema.isBlank())
                ? table
                : "\"" + schema + "\".\"" + table + "\"";

        StringBuilder sql = new StringBuilder("CREATE TABLE " + fullName + " (");

        for (ColumnMeta c : cols) {
            sql.append("\"")
               .append(c.getName())
               .append("\" ")
               .append(mapType(c, targetDbType));

            if (!c.isNullable()) {
                sql.append(" NOT NULL");
            }
            sql.append(",");
        }

        sql.setLength(sql.length() - 1);
        sql.append(")");

        return sql.toString();
    }

    /**
     * INSERT statement builder
     */
    public static String insertSql(String schema, String table, int colCount) {

        String fullName = (schema == null || schema.isBlank())
                ? "\"" + table + "\""
                : "\"" + schema + "\".\"" + table + "\"";

        return "INSERT INTO " + fullName +
                " VALUES (" +
                "?,".repeat(colCount).substring(0, colCount * 2 - 1) +
                ")";
    }
}
