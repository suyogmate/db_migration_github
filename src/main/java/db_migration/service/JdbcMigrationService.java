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
public class JdbcMigrationService {

    @Autowired DatabaseConfigRepository dbRepo;
    @Autowired MigrationJobRepository jobRepo;
    @Autowired MigrationLogRepository logRepo;

    public Long startMigration(Long srcId, Long tgtId) throws Exception {

        MigrationJob job = new MigrationJob();
        job.setSourceDbId(srcId);
        job.setTargetDbId(tgtId);
        job.setStatus("RUNNING");
        job.setStartedAt(LocalDateTime.now());
        jobRepo.save(job);

        DatabaseConfig srcCfg = dbRepo.findById(srcId).orElseThrow();
        DatabaseConfig tgtCfg = dbRepo.findById(tgtId).orElseThrow();

        try (
            Connection src = JdbcConnectionFactory.getConnection(srcCfg);
            Connection tgt = JdbcConnectionFactory.getConnection(tgtCfg)
        ) {

            tgt.setAutoCommit(false);

            List<String> tables = MetadataUtil.getTables(src, srcCfg.getSchema());

            for (String table : tables) {

                log(job.getId(), "Creating table: " + table);

                String ddl = SqlBuilder.buildCreateTable(
                        src, tgt, table, srcCfg, tgtCfg
                );

                tgt.createStatement().execute(ddl);

                copyData(src, tgt, table, srcCfg, tgtCfg);

                log(job.getId(), "Completed table: " + table);
            }

            tgt.commit();
            job.setStatus("SUCCESS");

        } catch (Exception e) {
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            job.setEndedAt(LocalDateTime.now());
            jobRepo.save(job);
        }

        return job.getId();
    }

    private void copyData(Connection src, Connection tgt,
                          String table, DatabaseConfig srcCfg,
                          DatabaseConfig tgtCfg) throws Exception {

        String sql = "SELECT * FROM " + srcCfg.getSchema() + "." + table;
        ResultSet rs = src.createStatement().executeQuery(sql);

        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();

        String insert = "INSERT INTO " + tgtCfg.getSchema() + "." + table +
                " VALUES (" + "?,".repeat(cols).substring(0, cols * 2 - 1) + ")";

        PreparedStatement ps = tgt.prepareStatement(insert);

        while (rs.next()) {
            for (int i = 1; i <= cols; i++) {
                ps.setObject(i, rs.getObject(i));
            }
            ps.addBatch();
        }

        ps.executeBatch();
    }

    private void log(Long jobId, String msg) {
        MigrationLog l = new MigrationLog();
        l.setJobId(jobId);
        l.setMessage(msg);
        logRepo.save(l);
    }
}
