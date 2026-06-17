package com.example.demo.service;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfExportServiceTest {

    @Mock
    private IdCardRenderService idCardRenderService;

    private Profile sampleProfile() {
        return Profile.builder()
                .uuid(UUID.randomUUID().toString())
                .registrationNumber("2026-ENG-001")
                .type(ProfileType.STUDENT)
                .fullName("Jane Doe")
                .barcodeType(BarcodeType.CODE_128)
                .build();
    }

    @Test
    void exportSingleProducesPdfBytes() {
        when(idCardRenderService.renderSingle(any(Profile.class))).thenReturn(
                "<html><body><h1>Jane Doe</h1></body></html>");
        PdfExportService pdfExportService = new PdfExportService(idCardRenderService);

        byte[] pdf = pdfExportService.exportSingle(sampleProfile());

        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void exportBatchProducesPdfBytes() {
        when(idCardRenderService.renderBatch(anyList())).thenReturn(
                "<html><body><h1>Batch</h1></body></html>");
        PdfExportService pdfExportService = new PdfExportService(idCardRenderService);

        byte[] pdf = pdfExportService.exportBatch(List.of(sampleProfile(), sampleProfile()));

        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }
}
