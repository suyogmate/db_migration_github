package db_migration.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

            for (String table : MetadataUtil.getTables(src,src.getSchema())) {
                log(job.getId(), "Migrating table: " + table);
                
                String createsql= SqlBuilder.createTable(tgt.getSchema(), table, MetadataUtil.getColumns(src, src.getSchema(), table));
                
                tgt.createStatement().execute(createsql);

                Statement st = src.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM " + src.getSchema() +"." + table);
                ResultSetMetaData md = rs.getMetaData();
                
                PreparedStatement ps =
                        tgt.prepareStatement(SqlBuilder.insertSql(src.getSchema(),table, md.getColumnCount()));

                while (rs.next()) {
                    for (int i = 1; i <= md.getColumnCount(); i++) {
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

    private void log(Long jobId, String msg) {
        MigrationLog l = new MigrationLog();
        l.setJobId(jobId);
        l.setMessage(msg);
        logRepo.save(l);
    }
}
