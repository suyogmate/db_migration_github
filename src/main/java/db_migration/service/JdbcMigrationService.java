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

    @Autowired private DatabaseConfigRepository dbRepo;
    @Autowired private MigrationJobRepository jobRepo;
    @Autowired private MigrationLogRepository logRepo;

    @Override
    public void startMigration(Long sourceId, Long targetId) throws Exception {

        MigrationJob job = new MigrationJob();
        job.setSourceDbId(sourceId);
        job.setTargetDbId(targetId);
        job.setStatus("RUNNING");
        job.setStartedAt(LocalDateTime.now());
        jobRepo.save(job);

        DatabaseConfig srcCfg = dbRepo.findById(sourceId).orElseThrow();
        DatabaseConfig tgtCfg = dbRepo.findById(targetId).orElseThrow();

        try (
            Connection src = JdbcConnectionFactory.getConnection(srcCfg);
            Connection tgt = JdbcConnectionFactory.getConnection(tgtCfg)
        ) {
            src.setAutoCommit(false);
            tgt.setAutoCommit(false);

            String sourceSchema = resolveSchema(srcCfg);
            String targetSchema = resolveSchema(tgtCfg);

            createSchemaIfMissing(tgt, targetSchema, tgtCfg.getDbType());

            for (String table : MetadataUtil.getTables(src, sourceSchema)) {

                log(job.getId(), "Migrating table: " + table);

                List<ColumnMeta> cols =
                        MetadataUtil.getColumns(src, sourceSchema, table);

                // CREATE TABLE
                String ddl = SqlBuilder.createTable(
                        targetSchema, table, cols, tgtCfg.getDbType()
                );
                try (Statement st = tgt.createStatement()) {
                    st.execute(ddl);
                }

                // INSERT DATA
                String insertSql =
                        SqlBuilder.insertSql(targetSchema, table, cols.size());

                try (
                    PreparedStatement ps = tgt.prepareStatement(insertSql);
                    Statement st = src.createStatement();
                    ResultSet rs = st.executeQuery(
                        buildSelect(sourceSchema, table))
                ) {
                    int batch = 0;
                    while (rs.next()) {
                        for (int i = 0; i < cols.size(); i++) {
                            ps.setObject(i + 1, rs.getObject(i + 1));
                        }
                        ps.addBatch();

                        if (++batch % 1000 == 0) {
                            ps.executeBatch();
                        }
                    }
                    ps.executeBatch();
                }

                tgt.commit();
                log(job.getId(), "Completed table: " + table);
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

    /* ====================== HELPERS ====================== */

    private String resolveSchema(DatabaseConfig cfg) {
        if (cfg.getSchema() != null && !cfg.getSchema().isBlank()) {
            return cfg.getSchema().toUpperCase();
        }
        return null;
    }

    private void createSchemaIfMissing(Connection conn,
                                       String schema,
                                       String targetDbType) throws SQLException {

        if (schema == null || schema.isBlank()) {
            return;
        }

        // Oracle: schema == USER → never create
        if ("oracle".equalsIgnoreCase(targetDbType)) {
            return;
        }

        String sql = "CREATE SCHEMA IF NOT EXISTS \"" + schema + "\"";
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }

    private String buildSelect(String schema, String table) {
        if (schema == null || schema.isBlank()) {
            return "SELECT * FROM \"" + table + "\"";
        }
        return "SELECT * FROM \"" + schema + "\".\"" + table + "\"";
    }

    private void log(Long jobId, String msg) {
        MigrationLog l = new MigrationLog();
        l.setJobId(jobId);
        l.setMessage(msg);
        logRepo.save(l);
    }
}
