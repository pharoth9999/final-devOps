package com.example.demo.service;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileBuilder;
import com.example.demo.model.ProfileType;
import com.example.demo.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ProfileBuilder profileBuilder;
    @Mock
    private RegistrationNumberGenerator registrationNumberGenerator;
    @Mock
    private PhotoStorageService photoStorageService;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void createAssignsUuidAndRegistrationNumberWhenMissing() {
        Profile toCreate = Profile.builder()
                .type(ProfileType.STUDENT)
                .fullName("Jane Doe")
                .department("Engineering")
                .barcodeType(BarcodeType.CODE_128)
                .build();
        when(registrationNumberGenerator.generate(ProfileType.STUDENT, "Engineering")).thenReturn("2026-ENG-001");
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Profile saved = profileService.create(toCreate);

        assertThat(saved.getUuid()).isNotBlank();
        assertThat(saved.getRegistrationNumber()).isEqualTo("2026-ENG-001");
        verify(profileRepository).save(toCreate);
    }

    @Test
    void createKeepsExplicitRegistrationNumber() {
        Profile toCreate = Profile.builder()
                .type(ProfileType.EMPLOYEE)
                .fullName("John Roe")
                .registrationNumber("2026-EMP-099")
                .build();
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Profile saved = profileService.create(toCreate);

        assertThat(saved.getRegistrationNumber()).isEqualTo("2026-EMP-099");
        verify(registrationNumberGenerator, never()).generate(any(), anyString());
    }

    @Test
    void deleteRemovesStoredPhotoBeforeDeletingProfile() {
        Profile existing = Profile.builder().id(1L).photoFileName("photo.jpg").build();
        when(profileRepository.findById(1L)).thenReturn(java.util.Optional.of(existing));

        profileService.delete(1L);

        verify(photoStorageService).delete("photo.jpg");
        verify(profileRepository).delete(existing);
    }
}
