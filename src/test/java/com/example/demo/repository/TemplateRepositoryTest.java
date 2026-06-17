package com.example.demo.repository;

import com.example.demo.model.Template;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TemplateRepositoryTest {

    @Autowired
    private TemplateRepository templateRepository;

    @Test
    void savesAndFindsByCode() {
        templateRepository.save(Template.builder()
                .code("CLASSIC")
                .name("Classic")
                .organizationName("Acme University")
                .build());

        assertThat(templateRepository.findByCode("CLASSIC")).isEmpty(); // TEMP: intentional failure to test Jenkins failure-email path
        assertThat(templateRepository.existsByCode("CLASSIC")).isTrue();
        assertThat(templateRepository.existsByCode("UNKNOWN")).isFalse();
    }

    @Test
    void findsByNameContaining() {
        templateRepository.save(Template.builder().code("A1").name("Blue Classic").build());
        templateRepository.save(Template.builder().code("A2").name("Red Modern").build());

        assertThat(templateRepository.findByNameContainingIgnoreCase("classic")).hasSize(1);
    }
}
