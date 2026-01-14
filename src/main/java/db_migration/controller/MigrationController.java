package db_migration.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import db_migration.model.MigrationJob;
import db_migration.model.MigrationLog;
import db_migration.repository.MigrationJobRepository;
import db_migration.repository.MigrationLogRepository;
import db_migration.service.JdbcMigrationService;


@Controller
@RequestMapping("/migration")
public class MigrationController {

    @Autowired JdbcMigrationService migrationService;
    @Autowired MigrationJobRepository jobRepo;
    @Autowired MigrationLogRepository logRepo;

    @PostMapping("/start")
    @ResponseBody
    public Long start(@RequestParam Long sourceId,
                      @RequestParam Long targetId) throws Exception {
        return migrationService.startMigration(sourceId, targetId);
    }

    @GetMapping("/job/{id}")
    @ResponseBody
    public MigrationJob job(@PathVariable Long id) {
        return jobRepo.findById(id).orElseThrow();
    }

    @GetMapping("/logs/{id}")
    @ResponseBody
    public List<MigrationLog> logs(@PathVariable Long id) {
        return logRepo.findByJobIdOrderByLogTime(id);
    }
}
