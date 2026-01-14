package db_migration.service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import db_migration.model.*;
import db_migration.repository.*;
import db_migration.util.*;

@Service
public class JdbcMigrationService implements MigrationService {

    @Autowired DatabaseConfigRepository dbRepo;
    @Autowired MigrationJobRepository jobRepo;
    @Autowired MigrationLogRepository logRepo;

    @Override
    public void startMigration(Long sourceId, Long targetId) throws Exception {

        MigrationJob job = new MigrationJob();
        job.setSourceDbId(sourceId);
        job.setTargetDbId(targetId);
        job.setStatus("RUNNING");
        job.setStartedAt(LocalDateTime.now());
        jobRepo.save(job);

        try {
            DatabaseConfig srcCfg = dbRepo.findById(sourceId).orElseThrow();
            DatabaseConfig tgtCfg = dbRepo.findById(targetId).orElseThrow();

            Connection src = JdbcConnectionFactory.getConnection(srcCfg);
            Connection tgt = JdbcConnectionFactory.getConnection(tgtCfg);

            String srcSchema = normalizeSchema(srcCfg);
            String tgtSchema = normalizeSchema(tgtCfg);

            createSchemaIfMissing(tgt, tgtCfg.getDbType(), tgtSchema);

            for (String table : MetadataUtil.getTables(src, srcSchema)) {

                log(job.getId(), "Migrating table: " + table);

                List<ColumnMeta> cols =
                        MetadataUtil.getColumns(src, srcSchema, table);

                if (!tableExists(tgt, tgtCfg.getDbType(), tgtSchema, table)) {
                    String ddl = SqlBuilder.createTable(
                            tgtSchema, table, cols, tgtCfg.getDbType()
                    );
                    tgt.createStatement().execute(ddl);
                }

                Statement st = src.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT * FROM " + srcSchema + "." + table
                );

                PreparedStatement ps = tgt.prepareStatement(
                        SqlBuilder.insertSql(tgtSchema, table, cols.size())
                );

                while (rs.next()) {
                    for (int i = 1; i <= cols.size(); i++) {
                        ps.setObject(i, rs.getObject(i));
                    }
                    ps.addBatch();
                }

                ps.executeBatch();
            }

            job.setStatus("SUCCESS");

        } catch (Exception e) {
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            job.setEndedAt(LocalDateTime.now());
            jobRepo.save(job);
        }
    }

    /* ------------------ HELPERS ------------------ */

    private String normalizeSchema(DatabaseConfig cfg) {
        return (cfg.getSchema() == null || cfg.getSchema().isBlank())
                ? cfg.getUsername().toUpperCase()
                : cfg.getSchema().toUpperCase();
    }

    private boolean tableExists(
            Connection conn,
            String dbType,
            String schema,
            String table) throws SQLException {

        if ("oracle".equalsIgnoreCase(dbType)) {
            String sql = """
                SELECT COUNT(*)
                FROM ALL_TABLES
                WHERE OWNER = ?
                  AND TABLE_NAME = ?
            """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, schema);
                ps.setString(2, table.toUpperCase());
                ResultSet rs = ps.executeQuery();
                rs.next();
                return rs.getInt(1) > 0;
            }
        } else {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, schema, table, null);
            return rs.next();
        }
    }

    private void createSchemaIfMissing(
            Connection conn,
            String dbType,
            String schema) throws SQLException {

        if ("oracle".equalsIgnoreCase(dbType)) {
            // Oracle schema == user (already exists)
            return;
        }

        try (Statement st = conn.createStatement()) {
            st.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
        }
    }

    private void log(Long jobId, String msg) {
        MigrationLog l = new MigrationLog();
        l.setJobId(jobId);
        l.setMessage(msg);
        logRepo.save(l);
    }
}
