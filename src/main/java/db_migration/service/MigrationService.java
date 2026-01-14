package db_migration.service;

public interface MigrationService {
    void startMigration(Long sourceId, Long targetId) throws Exception;
}
