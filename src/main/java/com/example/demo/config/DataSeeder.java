package com.example.demo.config;

import com.example.demo.model.Template;
import com.example.demo.repository.TemplateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final TemplateRepository templateRepository;

    public DataSeeder(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    public void run(String... args) {
        if (!templateRepository.existsByCode("DEFAULT")) {
            templateRepository.save(Template.builder()
                    .code("DEFAULT")
                    .name("Default Blue")
                    .organizationName("Acme University")
                    .layout("VERTICAL")
                    .primaryColor("#1d4ed8")
                    .secondaryColor("#e0e7ff")
                    .textColor("#111827")
                    .tagline("Official Identification Card")
                    .build());
        }
    }
}
