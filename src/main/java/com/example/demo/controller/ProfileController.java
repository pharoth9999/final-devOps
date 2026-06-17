package com.example.demo.controller;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.service.BarcodeService;
import com.example.demo.service.IdCardRenderService;
import com.example.demo.service.PdfExportService;
import com.example.demo.service.ProfileService;
import com.example.demo.service.QrCodeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final IdCardRenderService idCardRenderService;
    private final PdfExportService pdfExportService;
    private final QrCodeService qrCodeService;
    private final BarcodeService barcodeService;

    public ProfileController(ProfileService profileService,
                              IdCardRenderService idCardRenderService,
                              PdfExportService pdfExportService,
                              QrCodeService qrCodeService,
                              BarcodeService barcodeService) {
        this.profileService = profileService;
        this.idCardRenderService = idCardRenderService;
        this.pdfExportService = pdfExportService;
        this.qrCodeService = qrCodeService;
        this.barcodeService = barcodeService;
    }

    // ---- CRUD ----------------------------------------------------------

    @GetMapping
    public Page<Profile> search(@RequestParam(required = false) String q, Pageable pageable) {
        return profileService.search(q, pageable);
    }

    @GetMapping("/type/{type}")
    public List<Profile> findByType(@PathVariable ProfileType type) {
        return profileService.findByType(type);
    }

    @GetMapping("/{id}")
    public Profile findById(@PathVariable Long id) {
        return profileService.findById(id);
    }

    @GetMapping("/default")
    public Profile buildDefault(@RequestParam(defaultValue = "USER") ProfileType type) {
        return profileService.buildDefault(type);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Profile create(@Valid @RequestBody Profile profile) {
        return profileService.create(profile);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Profile> createBatch(@RequestBody List<Profile> profiles) {
        return profileService.createBatch(profiles);
    }

    @PutMapping("/{id}")
    public Profile update(@PathVariable Long id, @Valid @RequestBody Profile profile) {
        return profileService.update(id, profile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        profileService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Photo upload ----------------------------------------------------

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Profile uploadPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return profileService.uploadPhoto(id, file);
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {
        Profile profile = profileService.findById(id);
        byte[] photo = profileService.loadPhoto(id);
        MediaType mediaType = "image/png".equals(profile.getPhotoContentType())
                ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(mediaType).body(photo);
    }

    // ---- Live preview ------------------------------------------------------

    @GetMapping(value = "/{id}/preview", produces = MediaType.TEXT_HTML_VALUE)
    public String previewSaved(@PathVariable Long id) {
        return idCardRenderService.renderSingle(profileService.findById(id));
    }

    /** Instant preview of a draft (not yet persisted) profile, used while the user is still typing. */
    @PostMapping(value = "/preview", produces = MediaType.TEXT_HTML_VALUE)
    public String previewDraft(@RequestBody Profile draft) {
        if (draft.getUuid() == null || draft.getUuid().isBlank()) {
            draft.setUuid("draft-preview");
        }
        if (draft.getRegistrationNumber() == null || draft.getRegistrationNumber().isBlank()) {
            draft.setRegistrationNumber("PREVIEW");
        }
        return idCardRenderService.renderSingle(draft);
    }

    @GetMapping(value = "/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrCode(@PathVariable Long id) {
        Profile profile = profileService.findById(id);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
                .body(qrCodeService.generateForProfile(profile, 240));
    }

    @GetMapping(value = "/{id}/barcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> barcode(@PathVariable Long id) {
        Profile profile = profileService.findById(id);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
                .body(barcodeService.generateForProfile(profile, 300, 80));
    }

    @GetMapping("/verify/{uuid}")
    public Map<String, Object> verify(@PathVariable String uuid) {
        Profile profile = profileService.findByUuid(uuid);
        return Map.of(
                "valid", true,
                "uuid", profile.getUuid(),
                "registrationNumber", profile.getRegistrationNumber(),
                "fullName", profile.getFullName(),
                "type", profile.getType(),
                "expiryDate", profile.getExpiryDate());
    }

    // ---- PDF export --------------------------------------------------------

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        Profile profile = profileService.findById(id);
        byte[] pdf = pdfExportService.exportSingle(profile);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + profile.getRegistrationNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /** Batch ID card generation: merges every requested profile's card into one PDF. */
    @PostMapping(value = "/batch/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportBatchPdf(@RequestBody List<Long> ids) {
        List<Profile> profiles = profileService.findAllByIds(ids);
        byte[] pdf = pdfExportService.exportBatch(profiles);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"id-cards-batch.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
