package com.example.demo.service;

import com.example.demo.model.Profile;
import com.example.demo.model.Template;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Base64;
import java.util.List;

/**
 * Renders the Thymeleaf ID-card template into HTML, embedding the photo,
 * QR code and barcode as base64 data URIs so the markup is self-contained
 * for both the live preview (iframe) and the PDF export (html2pdf).
 */
@Service
public class IdCardRenderService {

    private final SpringTemplateEngine templateEngine;
    private final PhotoStorageService photoStorageService;
    private final QrCodeService qrCodeService;
    private final BarcodeService barcodeService;

    public IdCardRenderService(SpringTemplateEngine templateEngine,
                                PhotoStorageService photoStorageService,
                                QrCodeService qrCodeService,
                                BarcodeService barcodeService) {
        this.templateEngine = templateEngine;
        this.photoStorageService = photoStorageService;
        this.qrCodeService = qrCodeService;
        this.barcodeService = barcodeService;
    }

    public String renderSingle(Profile profile) {
        Context context = new Context();
        context.setVariable("card", toCardModel(profile));
        return templateEngine.process("idcard", context);
    }

    public String renderBatch(List<Profile> profiles) {
        Context context = new Context();
        context.setVariable("cards", profiles.stream().map(this::toCardModel).toList());
        return templateEngine.process("idcard-batch", context);
    }

    private CardModel toCardModel(Profile profile) {
        Template template = profile.getTemplate() != null ? profile.getTemplate() : defaultTemplate();

        String photoBase64 = null;
        if (profile.hasPhoto()) {
            byte[] photoBytes = photoStorageService.load(profile.getPhotoFileName());
            String mime = profile.getPhotoContentType() != null ? profile.getPhotoContentType() : "image/jpeg";
            photoBase64 = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(photoBytes);
        }

        String qrBase64 = "data:image/png;base64,"
                + Base64.getEncoder().encodeToString(qrCodeService.generateForProfile(profile, 180));

        String barcodeBase64 = null;
        if (profile.getRegistrationNumber() != null) {
            barcodeBase64 = "data:image/png;base64,"
                    + Base64.getEncoder().encodeToString(barcodeService.generateForProfile(profile, 260, 70));
        }

        return new CardModel(profile, template, photoBase64, qrBase64, barcodeBase64);
    }

    private Template defaultTemplate() {
        return Template.builder()
                .code("DEFAULT")
                .name("Default")
                .organizationName("Organization")
                .layout("VERTICAL")
                .primaryColor("#1d4ed8")
                .secondaryColor("#e0e7ff")
                .textColor("#111827")
                .build();
    }

    /** View model exposed to the Thymeleaf template. */
    public record CardModel(Profile profile, Template template, String photoBase64, String qrBase64,
                             String barcodeBase64) {
    }
}
