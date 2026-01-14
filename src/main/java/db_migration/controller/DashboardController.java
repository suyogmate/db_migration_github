package db_migration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import db_migration.repository.DatabaseConfigRepository;

@Controller
public class DashboardController {

    @Autowired
    private DatabaseConfigRepository dbRepo;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("dbs", dbRepo.findAll());
        return "dashboard";
    }
}
