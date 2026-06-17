package com.example.demo.model;

import com.example.demo.repository.TemplateRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Builds a default, unsaved {@link Profile} so a new ID-card form can be
 * pre-filled with sensible values (uuid, default template, validity window)
 * before the user fills in personal details.
 */
@Component
public class ProfileBuilder {

    private final TemplateRepository templateRepository;

    public ProfileBuilder(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public Profile buildDefault(ProfileType type) {
        ProfileType resolvedType = type == null ? ProfileType.USER : type;
        Template defaultTemplate = templateRepository.findByCode("DEFAULT").orElse(null);

        return Profile.builder()
                .uuid(UUID.randomUUID().toString())
                .type(resolvedType)
                .fullName("")
                .barcodeType(BarcodeType.CODE_128)
                .template(defaultTemplate)
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(defaultValidityYears(resolvedType)))
                .build();
    }

    private int defaultValidityYears(ProfileType type) {
        return switch (type) {
            case STUDENT -> 1;
            case EMPLOYEE -> 3;
            case USER -> 2;
        };
    }
}
