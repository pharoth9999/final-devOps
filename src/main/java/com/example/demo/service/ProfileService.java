package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileBuilder;
import com.example.demo.model.ProfileType;
import com.example.demo.repository.ProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileBuilder profileBuilder;
    private final RegistrationNumberGenerator registrationNumberGenerator;
    private final PhotoStorageService photoStorageService;

    public ProfileService(ProfileRepository profileRepository,
                           ProfileBuilder profileBuilder,
                           RegistrationNumberGenerator registrationNumberGenerator,
                           PhotoStorageService photoStorageService) {
        this.profileRepository = profileRepository;
        this.profileBuilder = profileBuilder;
        this.registrationNumberGenerator = registrationNumberGenerator;
        this.photoStorageService = photoStorageService;
    }

    @Transactional(readOnly = true)
    public Page<Profile> search(String query, Pageable pageable) {
        String term = query == null ? "" : query;
        return profileRepository
                .findByFullNameContainingIgnoreCaseOrRegistrationNumberContainingIgnoreCaseOrDepartmentContainingIgnoreCase(
                        term, term, term, pageable);
    }

    @Transactional(readOnly = true)
    public List<Profile> findByType(ProfileType type) {
        return profileRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public Profile findById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + id));
    }

    @Transactional(readOnly = true)
    public Profile findByUuid(String uuid) {
        return profileRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + uuid));
    }

    /** Builds (but does not persist) a default profile to pre-fill a creation form. */
    @Transactional(readOnly = true)
    public Profile buildDefault(ProfileType type) {
        return profileBuilder.buildDefault(type);
    }

    public Profile create(Profile profile) {
        if (profile.getUuid() == null || profile.getUuid().isBlank()) {
            profile.setUuid(UUID.randomUUID().toString());
        }
        if (profile.getRegistrationNumber() == null || profile.getRegistrationNumber().isBlank()) {
            profile.setRegistrationNumber(
                    registrationNumberGenerator.generate(profile.getType(), profile.getDepartment()));
        }
        profile.setId(null);
        return profileRepository.save(profile);
    }

    public List<Profile> createBatch(List<Profile> profiles) {
        return profiles.stream().map(this::create).toList();
    }

    public Profile update(Long id, Profile changes) {
        Profile existing = findById(id);
        existing.setType(changes.getType());
        existing.setFullName(changes.getFullName());
        existing.setDepartment(changes.getDepartment());
        existing.setTitle(changes.getTitle());
        existing.setEmail(changes.getEmail());
        existing.setPhone(changes.getPhone());
        existing.setBloodGroup(changes.getBloodGroup());
        existing.setDateOfBirth(changes.getDateOfBirth());
        existing.setIssueDate(changes.getIssueDate());
        existing.setExpiryDate(changes.getExpiryDate());
        existing.setTemplate(changes.getTemplate());
        existing.setBarcodeType(changes.getBarcodeType());
        return profileRepository.save(existing);
    }

    public void delete(Long id) {
        Profile existing = findById(id);
        photoStorageService.delete(existing.getPhotoFileName());
        profileRepository.delete(existing);
    }

    public Profile uploadPhoto(Long id, MultipartFile file) {
        Profile profile = findById(id);
        photoStorageService.delete(profile.getPhotoFileName());
        String storedFileName = photoStorageService.store(file);
        profile.setPhotoFileName(storedFileName);
        profile.setPhotoContentType(file.getContentType());
        return profileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public byte[] loadPhoto(Long id) {
        Profile profile = findById(id);
        if (!profile.hasPhoto()) {
            throw new ResourceNotFoundException("Profile has no photo: " + id);
        }
        return photoStorageService.load(profile.getPhotoFileName());
    }

    @Transactional(readOnly = true)
    public List<Profile> findAllByIds(List<Long> ids) {
        return profileRepository.findAllByIdIn(ids);
    }
}
