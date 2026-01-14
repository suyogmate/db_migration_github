package db_migration.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import db_migration.model.MigrationLog;
import db_migration.repository.MigrationLogRepository;

@RestController
@RequestMapping("/logs")
public class MigrationLogController {

    @Autowired
    private MigrationLogRepository logRepo;

    @GetMapping("/{jobId}")
    public List<MigrationLog> getLogs(@PathVariable Long jobId) {
        return logRepo.findByJobIdOrderByLogTime(jobId);
    }
}
