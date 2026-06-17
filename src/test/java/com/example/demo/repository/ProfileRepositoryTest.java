package com.example.demo.repository;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProfileRepositoryTest {

    @org.springframework.beans.factory.annotation.Autowired
    private ProfileRepository profileRepository;

    private Profile newProfile(String regNumber, String name) {
        return Profile.builder()
                .uuid(UUID.randomUUID().toString())
                .registrationNumber(regNumber)
                .type(ProfileType.STUDENT)
                .fullName(name)
                .department("Engineering")
                .barcodeType(BarcodeType.CODE_128)
                .build();
    }

    @Test
    void savesAndFindsByUuidAndRegistrationNumber() {
        Profile saved = profileRepository.save(newProfile("2026-ENG-001", "Jane Doe"));

        assertThat(profileRepository.findByUuid(saved.getUuid())).isPresent();
        assertThat(profileRepository.findByRegistrationNumber("2026-ENG-001")).isPresent();
        assertThat(profileRepository.existsByRegistrationNumber("2026-ENG-001")).isTrue();
        assertThat(profileRepository.existsByUuid(saved.getUuid())).isTrue();
    }

    @Test
    void countsByRegistrationNumberPrefix() {
        profileRepository.save(newProfile("2026-ENG-001", "Jane Doe"));
        profileRepository.save(newProfile("2026-ENG-002", "John Roe"));
        profileRepository.save(newProfile("2026-SCI-001", "Amy Lee"));

        assertThat(profileRepository.countByRegistrationNumberStartingWith("2026-ENG-")).isEqualTo(2);
    }

    @Test
    void searchesByNameRegistrationOrDepartment() {
        profileRepository.save(newProfile("2026-ENG-001", "Jane Doe"));
        profileRepository.save(newProfile("2026-SCI-002", "John Roe"));

        var page = profileRepository
                .findByFullNameContainingIgnoreCaseOrRegistrationNumberContainingIgnoreCaseOrDepartmentContainingIgnoreCase(
                        "jane", "jane", "jane", PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getFullName()).isEqualTo("Jane Doe");
    }
}
