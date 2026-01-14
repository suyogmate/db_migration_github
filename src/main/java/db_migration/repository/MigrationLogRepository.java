package db_migration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import db_migration.model.MigrationLog;

public interface MigrationLogRepository
extends JpaRepository<MigrationLog, Long> {

List<MigrationLog> findByJobIdOrderByLogTime(Long jobId);
}
