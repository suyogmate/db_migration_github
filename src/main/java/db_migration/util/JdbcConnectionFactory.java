package db_migration.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import db_migration.model.DatabaseConfig;

public class JdbcConnectionFactory {

    public static Connection getConnection(DatabaseConfig cfg) throws Exception {

        String url = switch (cfg.getDbType().toLowerCase()) {
            case "postgres" ->
                "jdbc:postgresql://" + cfg.getHost() + ":" + cfg.getPort() + "/" + cfg.getDbName();
            case "oracle" ->
                "jdbc:oracle:thin:@" + cfg.getHost() + ":" + cfg.getPort() + ":" + cfg.getDbName();
            default -> throw new RuntimeException("Unsupported DB: " + cfg.getDbType());
        };

        Properties props = new Properties();
        props.setProperty("user", cfg.getUsername());
        props.setProperty("password", cfg.getPassword());

        // 🔥 CRITICAL FIX
        if ("postgres".equalsIgnoreCase(cfg.getDbType())) {
            props.setProperty("TimeZone", "Asia/Kolkata"); // ✅ VALID
        }

        return DriverManager.getConnection(url, props);
    }
}
