package com.example.demo.service;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BarcodeServiceTest {

    private final BarcodeService barcodeService = new BarcodeService();

    @Test
    void generatesCode128Png() {
        byte[] png = barcodeService.generatePng("2026-ENG-001", BarcodeType.CODE_128, 300, 80);
        assertThat(isPng(png)).isTrue();
    }

    @Test
    void generatesEan13PngFromArbitraryDigits() {
        byte[] png = barcodeService.generatePng("12345", BarcodeType.EAN_13, 300, 80);
        assertThat(isPng(png)).isTrue();
    }

    @Test
    void generatesForProfileBasedOnBarcodeType() {
        Profile profile = Profile.builder()
                .id(42L)
                .uuid(UUID.randomUUID().toString())
                .registrationNumber("2026-ENG-001")
                .type(ProfileType.STUDENT)
                .fullName("Jane Doe")
                .barcodeType(BarcodeType.EAN_13)
                .build();

        byte[] png = barcodeService.generateForProfile(profile, 300, 80);
        assertThat(isPng(png)).isTrue();
    }

    private boolean isPng(byte[] bytes) {
        return bytes.length > 8
                && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G';
    }
}
