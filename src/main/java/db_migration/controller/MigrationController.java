package db_migration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import db_migration.service.MigrationService;

@Controller
public class MigrationController {

    @Autowired
    private MigrationService migrationService;

    @PostMapping("/migrate")
    public String start(
            @RequestParam Long sourceId,
            @RequestParam Long targetId) throws Exception {

        migrationService.startMigration(sourceId, targetId);
        return "redirect:/";
    }
}
