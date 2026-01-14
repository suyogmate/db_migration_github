package db_migration.util;

import java.sql.Types;
import java.util.List;

import db_migration.model.ColumnMeta;

public class SqlBuilder {

    public static String mapType(ColumnMeta c, String targetDb) {

        boolean oracle = "oracle".equalsIgnoreCase(targetDb);

        return switch (c.getJdbcType()) {

            case Types.INTEGER ->
                oracle ? "NUMBER(10)" : "INTEGER";

            case Types.BIGINT ->
                oracle ? "NUMBER(19)" : "BIGINT";

            case Types.VARCHAR, Types.CHAR ->
                oracle ? "VARCHAR2(" + Math.min(c.getSize(), 4000) + ")"
                       : "VARCHAR(" + c.getSize() + ")";

            case Types.DATE ->
                oracle ? "DATE" : "DATE";

            case Types.TIMESTAMP ->
                oracle ? "TIMESTAMP" : "TIMESTAMP";

            case Types.NUMERIC, Types.DECIMAL ->
                oracle ? "NUMBER" : "NUMERIC";

            default ->
                oracle ? "CLOB" : "TEXT";
        };
    }

    public static String createTable(
            String schema,
            String table,
            List<ColumnMeta> cols,
            String targetDb) {

        boolean oracle = "oracle".equalsIgnoreCase(targetDb);

        StringBuilder sql = new StringBuilder();

        // Oracle does NOT support IF NOT EXISTS
        if (oracle) {
            sql.append("CREATE TABLE ");
        } else {
            sql.append("CREATE TABLE IF NOT EXISTS ");
        }

        if (schema != null && !schema.isBlank()) {
            sql.append(schema).append(".").append(table);
        } else {
            sql.append(table);
        }

        sql.append(" (");

        for (ColumnMeta c : cols) {
            sql.append("\"").append(c.getName()).append("\" ")
               .append(mapType(c, targetDb))
               .append(c.isNullable() ? "" : " NOT NULL")
               .append(", ");
        }

        sql.setLength(sql.length() - 2);
        sql.append(")");

        return sql.toString();
    }

    public static String insertSql(String schema, String table, int colCount) {

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");

        if (schema != null && !schema.isBlank()) {
            sb.append(schema).append(".").append(table);
        } else {
            sb.append(table);
        }

        sb.append(" VALUES (");

        for (int i = 0; i < colCount; i++) {
            sb.append("?");
            if (i < colCount - 1) sb.append(",");
        }

        sb.append(")");
        return sb.toString();
    }
}
