package db_migration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import db_migration.repository.MigrationJobRepository;

@Controller
public class DashboardController {

    @Autowired
    private MigrationJobRepository jobRepo;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("jobs", jobRepo.findAll());
        return "dashboard";
    }
}
