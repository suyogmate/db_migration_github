package db_migration.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import db_migration.model.ColumnMeta;
import db_migration.model.DatabaseConfig;
import db_migration.model.MigrationJob;
import db_migration.model.MigrationLog;
import db_migration.repository.DatabaseConfigRepository;
import db_migration.repository.MigrationJobRepository;
import db_migration.repository.MigrationLogRepository;
import db_migration.util.JdbcConnectionFactory;
import db_migration.util.MetadataUtil;
import db_migration.util.SqlBuilder;

@Service
public class JdbcMigrationService implements MigrationService {

    @Autowired
    private DatabaseConfigRepository dbRepo;

    @Autowired
    private MigrationJobRepository jobRepo;

    @Autowired
    private MigrationLogRepository logRepo;

    @Override
    public void startMigration(Long sourceId, Long targetId) throws Exception {

        MigrationJob job = new MigrationJob();
        job.setSourceDbId(sourceId);
        job.setTargetDbId(targetId);
        job.setStatus("RUNNING");
        job.setStartedAt(LocalDateTime.now());
        jobRepo.save(job);

        Connection src = null;
        Connection tgt = null;

        try {
            DatabaseConfig srcCfg = dbRepo.findById(sourceId).orElseThrow();
            DatabaseConfig tgtCfg = dbRepo.findById(targetId).orElseThrow();

            src = JdbcConnectionFactory.getConnection(srcCfg);
            tgt = JdbcConnectionFactory.getConnection(tgtCfg);

            String srcSchema = resolveSchema(srcCfg);
            String tgtSchema = resolveSchema(tgtCfg);

            List<String> tables = MetadataUtil.getTables(src, srcSchema);

            for (String table : tables) {

                log(job.getId(), "Migrating table: " + table);

                /* ---------- CREATE TABLE ---------- */

                List<ColumnMeta> cols = MetadataUtil.getColumns(src, srcSchema, table);

                String createSql = SqlBuilder.createTable(
                        tgtSchema,
                        table,
                        cols,
                        tgtCfg.getDbType()
                );

                try (Statement ddl = tgt.createStatement()) {
                    ddl.execute(createSql);
                }

                /* ---------- DATA COPY ---------- */

                String selectSql = "SELECT * FROM " + srcSchema + "." + table;
                Statement st = src.createStatement();
                ResultSet rs = st.executeQuery(selectSql);
                ResultSetMetaData md = rs.getMetaData();

                String insertSql = SqlBuilder.insertSql(
                        tgtSchema,
                        table,
                        md.getColumnCount()
                );

                PreparedStatement ps = tgt.prepareStatement(insertSql);

                int batch = 0;
                while (rs.next()) {
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        ps.setObject(i, rs.getObject(i));
                    }
                    ps.addBatch();

                    if (++batch % 500 == 0) {
                        ps.executeBatch();
                    }
                }
                ps.executeBatch();

                rs.close();
                st.close();
                ps.close();
            }

            job.setStatus("SUCCESS");

        } catch (Exception e) {
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            throw e;

        } finally {
            job.setEndedAt(LocalDateTime.now());
            jobRepo.save(job);

            if (src != null) src.close();
            if (tgt != null) tgt.close();
        }
    }

    /* ---------------- HELPERS ---------------- */

    private String resolveSchema(DatabaseConfig cfg) {
        if (cfg.getSchema() != null && !cfg.getSchema().isBlank()) {
            return cfg.getSchema().toUpperCase();
        }

        if ("oracle".equalsIgnoreCase(cfg.getDbType())) {
            return cfg.getUsername().toUpperCase();
        }

        return "public";
    }

    private void log(Long jobId, String msg) {
        MigrationLog l = new MigrationLog();
        l.setJobId(jobId);
        l.setMessage(msg);
        logRepo.save(l);
    }
}
