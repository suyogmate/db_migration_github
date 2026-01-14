package db_migration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import db_migration.model.DatabaseConfig;
import db_migration.repository.DatabaseConfigRepository;

@Controller
@RequestMapping("/db")
public class DatabaseConfigController {

    @Autowired
    private DatabaseConfigRepository dbRepo;

    @PostMapping("/save")
    public String save(@ModelAttribute DatabaseConfig config) {
        dbRepo.save(config);
        return "redirect:/";
    }
}
