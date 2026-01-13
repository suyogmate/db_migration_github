package db_migration.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import db_migration.model.DatabaseConfig;
import db_migration.repository.DatabaseConfigRepository;

@RestController
@RequestMapping("/api/databases")
public class DatabaseConfigController {

    @Autowired DatabaseConfigRepository repo;

    @PostMapping
    public void save(@RequestBody DatabaseConfig cfg) {
        if (cfg.getName() == null || cfg.getName().isBlank()) {
            cfg.setName(cfg.getDbName()); // fallback
        }
        repo.save(cfg);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }

    @GetMapping
    public List<DatabaseConfig> list() {
        return repo.findAll();
    }
}
