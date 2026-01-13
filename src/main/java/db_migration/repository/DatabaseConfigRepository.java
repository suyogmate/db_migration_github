package db_migration.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import db_migration.model.DatabaseConfig;

public interface DatabaseConfigRepository
        extends JpaRepository<DatabaseConfig, Long> {}