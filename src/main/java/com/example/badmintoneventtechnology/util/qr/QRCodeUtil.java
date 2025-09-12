package com.example.badmintoneventtechnology.util.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;

public final class QRCodeUtil {
    private QRCodeUtil() {
    }

    public static BufferedImage generate(String text, int size) throws WriterException {
        var writer = new QRCodeWriter();
        var matrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);
        var img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                img.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return img;
    }
}