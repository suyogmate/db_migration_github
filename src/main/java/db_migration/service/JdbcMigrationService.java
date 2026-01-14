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

        try (
            Connection src = JdbcConnectionFactory.getConnection(
                    dbRepo.findById(sourceId).orElseThrow());
            Connection tgt = JdbcConnectionFactory.getConnection(
                    dbRepo.findById(targetId).orElseThrow())
        ) {

            DatabaseConfig srcCfg = dbRepo.findById(sourceId).orElseThrow();
            DatabaseConfig tgtCfg = dbRepo.findById(targetId).orElseThrow();

            String srcSchema = normalizeSchema(srcCfg);
            String tgtSchema = normalizeSchema(tgtCfg);

            for (String table : MetadataUtil.getTables(src, srcSchema)) {

                log(job.getId(), "Processing table: " + table);

                List<ColumnMeta> cols =
                        MetadataUtil.getColumns(src, srcSchema, table);

                createTableIfMissing(tgt, tgtCfg.getDbType(),
                        tgtSchema, table, cols);

                migrateData(src, tgt, srcSchema, tgtSchema, table, cols);
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

    /* ---------------- helpers ---------------- */

    private void migrateData(Connection src, Connection tgt,
                             String srcSchema, String tgtSchema,
                             String table, List<ColumnMeta> cols) throws Exception {

        String selectSql = "SELECT * FROM " + srcSchema + "." + table;
        String insertSql = SqlBuilder.insertSql(tgtSchema, table, cols);

        try (
            Statement st = src.createStatement();
            ResultSet rs = st.executeQuery(selectSql);
            PreparedStatement ps = tgt.prepareStatement(insertSql)
        ) {
            while (rs.next()) {
                for (int i = 0; i < cols.size(); i++) {
                    ps.setObject(i + 1, rs.getObject(i + 1));
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void createTableIfMissing(Connection conn, String dbType,
                                      String schema, String table,
                                      List<ColumnMeta> cols) throws Exception {

        if (MetadataUtil.tableExists(conn, schema, table)) return;

        String ddl = SqlBuilder.createTable(dbType, schema, table, cols);
        try (Statement st = conn.createStatement()) {
            st.execute(ddl);
        }
    }

    private String normalizeSchema(DatabaseConfig cfg) {
        if (cfg.getSchema() != null && !cfg.getSchema().isBlank()) {
            return cfg.getSchema();
        }
        return cfg.getDbType().equalsIgnoreCase("oracle")
                ? cfg.getUsername().toUpperCase()
                : "public";
    }

    private void log(Long jobId, String msg) {
        MigrationLog l = new MigrationLog();
        l.setJobId(jobId);
        l.setMessage(msg);
        logRepo.save(l);
    }
}
