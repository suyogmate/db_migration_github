package db_migration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import db_migration.service.MigrationService;



@RestController
@RequestMapping("/api/migration")
public class MigrationController {

    @Autowired MigrationService migrationService;

    @PostMapping("/start")
    public String start(@RequestParam Long sourceDbId,
                        @RequestParam Long targetDbId) throws Exception {
        migrationService.startMigration(sourceDbId, targetDbId);
        return "Migration started";
    }
}
