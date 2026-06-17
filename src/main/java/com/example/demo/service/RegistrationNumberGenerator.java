package com.example.demo.service;

import com.example.demo.model.ProfileType;
import com.example.demo.repository.ProfileRepository;
import org.springframework.stereotype.Component;

import java.time.Year;

/**
 * Generates human-friendly registration numbers in the format
 * {@code YEAR-DEPT-###}, e.g. {@code 2026-ENG-014}, falling back to a
 * generic department code when none is supplied.
 */
@Component
public class RegistrationNumberGenerator {

    private final ProfileRepository profileRepository;

    public RegistrationNumberGenerator(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public String generate(ProfileType type, String department) {
        String year = String.valueOf(Year.now().getValue());
        String deptCode = deptCode(type, department);
        String prefix = year + "-" + deptCode + "-";

        long sequence = profileRepository.countByRegistrationNumberStartingWith(prefix) + 1;
        String candidate = prefix + pad(sequence);
        while (profileRepository.existsByRegistrationNumber(candidate)) {
            sequence++;
            candidate = prefix + pad(sequence);
        }
        return candidate;
    }

    private String deptCode(ProfileType type, String department) {
        if (department != null && !department.isBlank()) {
            String letters = department.replaceAll("[^A-Za-z]", "").toUpperCase();
            if (letters.length() >= 3) {
                return letters.substring(0, 3);
            }
            if (!letters.isEmpty()) {
                return letters;
            }
        }
        return type == null ? "GEN" : switch (type) {
            case STUDENT -> "STU";
            case EMPLOYEE -> "EMP";
            case USER -> "USR";
        };
    }

    private String pad(long sequence) {
        return String.format("%03d", sequence);
    }
}
