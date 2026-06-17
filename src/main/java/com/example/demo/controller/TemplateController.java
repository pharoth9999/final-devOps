package com.example.demo.controller;

import com.example.demo.model.Template;
import com.example.demo.service.TemplateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public List<Template> findAll() {
        return templateService.findAll();
    }

    @GetMapping("/{id}")
    public Template findById(@PathVariable Long id) {
        return templateService.findById(id);
    }

    @GetMapping("/code/{code}")
    public Template findByCode(@PathVariable String code) {
        return templateService.findByCode(code);
    }

    @GetMapping("/exists/{code}")
    public Map<String, Boolean> exists(@PathVariable String code) {
        return Map.of("exists", templateService.existsByCode(code));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Template create(@Valid @RequestBody Template template) {
        return templateService.create(template);
    }

    @PutMapping("/{id}")
    public Template update(@PathVariable Long id, @Valid @RequestBody Template template) {
        return templateService.update(id, template);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
