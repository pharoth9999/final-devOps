package com.example.demo.controller;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.service.BarcodeService;
import com.example.demo.service.IdCardRenderService;
import com.example.demo.service.PdfExportService;
import com.example.demo.service.ProfileService;
import com.example.demo.service.QrCodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfileService profileService;
    @MockitoBean
    private IdCardRenderService idCardRenderService;
    @MockitoBean
    private PdfExportService pdfExportService;
    @MockitoBean
    private QrCodeService qrCodeService;
    @MockitoBean
    private BarcodeService barcodeService;

    private Profile sample() {
        return Profile.builder()
                .id(1L)
                .uuid(UUID.randomUUID().toString())
                .registrationNumber("2026-ENG-001")
                .type(ProfileType.STUDENT)
                .fullName("Jane Doe")
                .barcodeType(BarcodeType.CODE_128)
                .build();
    }

    @Test
    void createReturnsCreatedProfile() throws Exception {
        Profile request = Profile.builder().type(ProfileType.STUDENT).fullName("Jane Doe").build();
        when(profileService.create(any(Profile.class))).thenReturn(sample());

        mockMvc.perform(post("/api/profiles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationNumber").value("2026-ENG-001"));
    }

    @Test
    void createWithoutFullNameReturnsValidationError() throws Exception {
        Profile request = Profile.builder().type(ProfileType.STUDENT).build();

        mockMvc.perform(post("/api/profiles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.fullName").exists());
    }

    @Test
    void findByIdReturns404WhenMissing() throws Exception {
        when(profileService.findById(99L)).thenThrow(new ResourceNotFoundException("Profile not found: 99"));

        mockMvc.perform(get("/api/profiles/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchReturnsPagedResults() throws Exception {
        when(profileService.search(any(), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(sample()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/profiles").param("q", "jane"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("Jane Doe"));
    }

    @Test
    void previewDraftReturnsHtml() throws Exception {
        when(idCardRenderService.renderSingle(any(Profile.class))).thenReturn("<html>card</html>");

        mockMvc.perform(post("/api/profiles/preview")
                        .contentType("application/json")
                        .content("{\"type\":\"STUDENT\",\"fullName\":\"Jane Doe\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("<html>card</html>"));
    }

    @Test
    void exportPdfReturnsPdfContentType() throws Exception {
        when(profileService.findById(1L)).thenReturn(sample());
        when(pdfExportService.exportSingle(any(Profile.class))).thenReturn(new byte[] {'%', 'P', 'D', 'F'});

        mockMvc.perform(get("/api/profiles/1/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));
    }
}
