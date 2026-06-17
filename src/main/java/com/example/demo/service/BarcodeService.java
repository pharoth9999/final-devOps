package com.example.demo.service;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.EAN13Writer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Generates linear barcodes (Code-128 / EAN-13) with ZXing for the
 * profile's registration number / id.
 */
@Service
public class BarcodeService {

    public byte[] generateForProfile(Profile profile, int width, int height) {
        BarcodeType type = profile.getBarcodeType() == null ? BarcodeType.CODE_128 : profile.getBarcodeType();
        String content = type == BarcodeType.EAN_13
                ? toEan13Digits(profile.getId())
                : profile.getRegistrationNumber();
        return generatePng(content, type, width, height);
    }

    public byte[] generatePng(String content, BarcodeType type, int width, int height) {
        BitMatrix matrix = switch (type) {
            case CODE_128 -> new Code128Writer().encode(content, BarcodeFormat.CODE_128, width, height);
            case EAN_13 -> new EAN13Writer().encode(toEan13Digits(content), BarcodeFormat.EAN_13, width, height);
        };
        return toPng(matrix);
    }

    /** EAN-13 needs exactly 12 numeric digits (the 13th checksum digit is computed by ZXing). */
    private String toEan13Digits(Long seed) {
        String digits = seed == null ? "1" : String.valueOf(Math.abs(seed));
        return toEan13Digits(digits);
    }

    private String toEan13Digits(String raw) {
        String digitsOnly = raw == null ? "" : raw.replaceAll("\\D", "");
        if (digitsOnly.isEmpty()) {
            digitsOnly = "0";
        }
        if (digitsOnly.length() > 12) {
            digitsOnly = digitsOnly.substring(digitsOnly.length() - 12);
        }
        return "0".repeat(12 - digitsOnly.length()) + digitsOnly;
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
