package com.example.demo.service;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QrCodeServiceTest {

    private final QrCodeService qrCodeService = new QrCodeService("http://localhost:8080");

    @Test
    void generatesPngBytes() {
        byte[] png = qrCodeService.generatePng("hello world", 120);

        assertThat(png).isNotEmpty();
        assertThat(isPng(png)).isTrue();
    }

    @Test
    void buildsVerificationUrlAndEncodesProfile() {
        Profile profile = Profile.builder()
                .uuid(UUID.randomUUID().toString())
                .registrationNumber("2026-ENG-001")
                .type(ProfileType.STUDENT)
                .fullName("Jane Doe")
                .barcodeType(BarcodeType.CODE_128)
                .build();

        String url = qrCodeService.verificationUrl(profile);
        assertThat(url).isEqualTo("http://localhost:8080/api/profiles/verify/" + profile.getUuid());

        byte[] png = qrCodeService.generateForProfile(profile, 150);
        assertThat(isPng(png)).isTrue();
    }

    private boolean isPng(byte[] bytes) {
        return bytes.length > 8
                && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G';
    }
}
