package com.example.demo.service;

import com.example.demo.exception.InvalidPhotoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Validates and stores ID-card photos on local disk under the configured
 * upload directory, keyed by a random file name to avoid collisions.
 */
@Service
public class PhotoStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    private final Path uploadDir;
    private final long maxSizeBytes;

    public PhotoStorageService(@Value("${app.upload.dir:uploads/photos}") String uploadDir,
                                @Value("${app.upload.max-size-bytes:2097152}") long maxSizeBytes) {
        this.uploadDir = Path.of(uploadDir);
        this.maxSizeBytes = maxSizeBytes;
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create upload directory: " + this.uploadDir, e);
        }
    }

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidPhotoException("Photo file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidPhotoException("Only JPEG and PNG images are allowed");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new InvalidPhotoException("Photo exceeds the maximum allowed size of " + maxSizeBytes + " bytes");
        }
    }

    public String store(MultipartFile file) {
        validate(file);
        String extension = file.getContentType().equals("image/png") ? ".png" : ".jpg";
        String storedFileName = UUID.randomUUID() + extension;
        try {
            Files.copy(file.getInputStream(), uploadDir.resolve(storedFileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store photo", e);
        }
        return storedFileName;
    }

    public byte[] load(String fileName) {
        try {
            return Files.readAllBytes(uploadDir.resolve(fileName));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load photo: " + fileName, e);
        }
    }

    public void delete(String fileName) {
        if (fileName == null) {
            return;
        }
        try {
            Files.deleteIfExists(uploadDir.resolve(fileName));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete photo: " + fileName, e);
        }
    }
}
