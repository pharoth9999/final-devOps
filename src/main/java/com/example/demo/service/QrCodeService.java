package com.example.demo.service;

import com.example.demo.model.Profile;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Generates QR codes (ZXing) that embed a verification URL plus key
 * profile details, used on the card and for live preview.
 */
@Service
public class QrCodeService {

    private final String publicBaseUrl;

    public QrCodeService(@Value("${app.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public String verificationUrl(Profile profile) {
        return publicBaseUrl + "/api/profiles/verify/" + profile.getUuid();
    }

    public byte[] generateForProfile(Profile profile, int size) {
        String payload = "ID: " + profile.getRegistrationNumber()
                + "\nName: " + profile.getFullName()
                + "\nType: " + profile.getType()
                + "\nVerify: " + verificationUrl(profile);
        return generatePng(payload, size);
    }

    public byte[] generatePng(String content, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 1);
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            return toPng(matrix);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate QR code", e);
        }
    }

    private byte[] toPng(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
