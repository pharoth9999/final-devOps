package com.example.demo.service;

import com.example.demo.model.ProfileType;
import com.example.demo.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationNumberGeneratorTest {

    @Mock
    private ProfileRepository profileRepository;

    @Test
    void generatesYearDeptSequenceFormat() {
        RegistrationNumberGenerator generator = new RegistrationNumberGenerator(profileRepository);
        when(profileRepository.countByRegistrationNumberStartingWith(anyString())).thenReturn(2L);
        when(profileRepository.existsByRegistrationNumber(anyString())).thenReturn(false);

        String regNumber = generator.generate(ProfileType.STUDENT, "Engineering");

        assertThat(regNumber).isEqualTo(Year.now().getValue() + "-ENG-003");
    }

    @Test
    void fallsBackToTypeCodeWhenNoDepartment() {
        RegistrationNumberGenerator generator = new RegistrationNumberGenerator(profileRepository);
        when(profileRepository.countByRegistrationNumberStartingWith(anyString())).thenReturn(0L);
        when(profileRepository.existsByRegistrationNumber(anyString())).thenReturn(false);

        String regNumber = generator.generate(ProfileType.EMPLOYEE, null);

        assertThat(regNumber).isEqualTo(Year.now().getValue() + "-EMP-001");
    }

    @Test
    void skipsCollidingNumbers() {
        RegistrationNumberGenerator generator = new RegistrationNumberGenerator(profileRepository);
        when(profileRepository.countByRegistrationNumberStartingWith(anyString())).thenReturn(0L);
        when(profileRepository.existsByRegistrationNumber(Year.now().getValue() + "-USR-001")).thenReturn(true);
        when(profileRepository.existsByRegistrationNumber(Year.now().getValue() + "-USR-002")).thenReturn(false);

        String regNumber = generator.generate(ProfileType.USER, null);

        assertThat(regNumber).isEqualTo(Year.now().getValue() + "-USR-002");
    }
}
