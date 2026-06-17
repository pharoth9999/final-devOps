package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Template;
import com.example.demo.repository.TemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TemplateService {

    private final TemplateRepository templateRepository;

    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional(readOnly = true)
    public List<Template> findAll() {
        return templateRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Template findById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
    }

    @Transactional(readOnly = true)
    public Template findByCode(String code) {
        return templateRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + code));
    }

    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return templateRepository.existsByCode(code);
    }

    public Template create(Template template) {
        if (templateRepository.existsByCode(template.getCode())) {
            throw new IllegalArgumentException("Template code already exists: " + template.getCode());
        }
        template.setId(null);
        return templateRepository.save(template);
    }

    public Template update(Long id, Template changes) {
        Template existing = findById(id);
        existing.setName(changes.getName());
        existing.setOrganizationName(changes.getOrganizationName());
        existing.setLayout(changes.getLayout());
        existing.setPrimaryColor(changes.getPrimaryColor());
        existing.setSecondaryColor(changes.getSecondaryColor());
        existing.setTextColor(changes.getTextColor());
        existing.setTagline(changes.getTagline());
        return templateRepository.save(existing);
    }

    public void delete(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Template not found: " + id);
        }
        templateRepository.deleteById(id);
    }
}
