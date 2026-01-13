package db_migration.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import db_migration.model.MigrationJob;

public interface MigrationJobRepository
        extends JpaRepository<MigrationJob, Long> {}