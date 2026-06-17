package com.example.demo.repository;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUuid(String uuid);

    Optional<Profile> findByRegistrationNumber(String registrationNumber);

    boolean existsByUuid(String uuid);

    boolean existsByRegistrationNumber(String registrationNumber);

    List<Profile> findByType(ProfileType type);

    long countByRegistrationNumberStartingWith(String prefix);

    Page<Profile> findByFullNameContainingIgnoreCaseOrRegistrationNumberContainingIgnoreCaseOrDepartmentContainingIgnoreCase(
            String fullName, String registrationNumber, String department, Pageable pageable);

    List<Profile> findAllByIdIn(List<Long> ids);
}
