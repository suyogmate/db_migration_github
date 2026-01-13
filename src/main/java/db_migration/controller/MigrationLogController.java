package db_migration.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import db_migration.model.MigrationLog;
import db_migration.repository.MigrationLogRepository;

@RestController
@RequestMapping("/api/logs")
public class MigrationLogController {

    private final MigrationLogRepository repo;

    public MigrationLogController(MigrationLogRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/{jobId}")
    public List<MigrationLog> logs(@PathVariable Long jobId) {
        return repo.findByJobIdOrderByLogTime(jobId);
    }
}
