package db_migration.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import db_migration.repository.DatabaseConfigRepository;

@Controller
public class UiController {

    private final DatabaseConfigRepository dbRepo;

    public UiController(DatabaseConfigRepository dbRepo) {
        this.dbRepo = dbRepo;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("databases", dbRepo.findAll());
        return "dashboard";
    }
}