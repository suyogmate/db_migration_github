package db_migration.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import db_migration.model.MigrationLog;
import db_migration.repository.MigrationLogRepository;
import db_migration.service.MigrationService;

@RestController
@RequestMapping("/api/migration")
public class MigrationApiController {

    @Autowired
    private MigrationService migrationService;

    @Autowired
    private MigrationLogRepository logRepo;

    @PostMapping("/start")
    public void start(@RequestParam Long sourceId,
                      @RequestParam Long targetId) throws Exception {
        migrationService.startMigration(sourceId, targetId);
    }

    @GetMapping("/logs/{jobId}")
    public List<MigrationLog> logs(@PathVariable Long jobId) {
        return logRepo.findByJobIdOrderByLogTime(jobId);
    }
}
