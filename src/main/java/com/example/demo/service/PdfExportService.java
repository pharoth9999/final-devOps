package com.example.demo.service;

import com.example.demo.model.Profile;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Converts the Thymeleaf-rendered ID-card HTML straight to PDF using
 * iText's html2pdf, so the PDF and the live HTML preview always match.
 */
@Service
public class PdfExportService {

    private final IdCardRenderService idCardRenderService;

    public PdfExportService(IdCardRenderService idCardRenderService) {
        this.idCardRenderService = idCardRenderService;
    }

    public byte[] exportSingle(Profile profile) {
        String html = idCardRenderService.renderSingle(profile);
        return convert(html);
    }

    /** Generates one multi-page PDF containing every profile's card (batch export). */
    public byte[] exportBatch(List<Profile> profiles) {
        String html = idCardRenderService.renderBatch(profiles);
        return convert(html);
    }

    private byte[] convert(String html) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, out);
        return out.toByteArray();
    }
}
