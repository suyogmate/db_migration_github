package db_migration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import db_migration.model.MigrationJob;
import db_migration.repository.MigrationJobRepository;

@RestController
@RequestMapping("/job")
public class JobStatusController {

    @Autowired
    private MigrationJobRepository jobRepo;

    @GetMapping("/{id}")
    public MigrationJob status(@PathVariable Long id) {
        return jobRepo.findById(id).orElseThrow();
    }
}
