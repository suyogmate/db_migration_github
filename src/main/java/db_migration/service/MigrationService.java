package db_migration.service;

public interface MigrationService {

    /**
     * Starts migration from source database to target database
     */
    void startMigration(Long sourceDbId, Long targetDbId) throws Exception;
}