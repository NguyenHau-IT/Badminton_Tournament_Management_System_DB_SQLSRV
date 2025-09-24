package com.example.btms.util.image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class để resize ảnh sang các kích thước khác nhau
 */
public class ImageResizer {

    private static final String SOURCE_IMAGE = "avatar.png";
    private static final String OUTPUT_PREFIX = "app-";
    private static final String OUTPUT_EXTENSION = ".png";

    // Các kích thước cần tạo
    private static final int[] SIZES = { 16, 32, 48, 64, 128, 256 };

    public static void main(String[] args) {
        try {
            ImageResizer resizer = new ImageResizer();
            resizer.resizeAllImages();
            System.out.println("Đã resize thành công tất cả ảnh!");
        } catch (Exception e) {
            System.err.println("Lỗi khi resize ảnh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Resize tất cả ảnh sang các kích thước khác nhau
     */
    public void resizeAllImages() throws IOException {
        // Lấy đường dẫn đến thư mục icons
        Path iconsPath = getIconsPath();
        File sourceFile = iconsPath.resolve(SOURCE_IMAGE).toFile();

        if (!sourceFile.exists()) {
            throw new IOException("Không tìm thấy file nguồn: " + sourceFile.getAbsolutePath());
        }

        // Đọc ảnh gốc
        BufferedImage sourceImage = ImageIO.read(sourceFile);
        System.out.println("Đã đọc ảnh gốc: " + sourceFile.getAbsolutePath());
        System.out.println("Kích thước gốc: " + sourceImage.getWidth() + "x" + sourceImage.getHeight());

        // Resize cho từng kích thước
        for (int size : SIZES) {
            resizeImage(sourceImage, iconsPath, size);
        }
    }

    /**
     * Resize ảnh sang kích thước cụ thể
     */
    private void resizeImage(BufferedImage source, Path outputPath, int size) throws IOException {
        // Tạo ảnh mới với kích thước mong muốn
        BufferedImage resized = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        // Vẽ ảnh gốc lên ảnh mới với kích thước mới
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(source, 0, 0, size, size, null);
        g2d.dispose();

        // Tạo tên file output
        String outputFileName = OUTPUT_PREFIX + size + OUTPUT_EXTENSION;
        File outputFile = outputPath.resolve(outputFileName).toFile();

        // Lưu ảnh
        ImageIO.write(resized, "PNG", outputFile);
        System.out.println("Đã tạo: " + outputFileName + " (" + size + "x" + size + ")");
    }

    /**
     * Lấy đường dẫn đến thư mục icons
     */
    private Path getIconsPath() {
        // Tìm thư mục icons từ classpath
        try {
            // Thử tìm từ resources
            String resourcePath = "/icons/" + SOURCE_IMAGE;
            if (getClass().getResource(resourcePath) != null) {
                // Nếu chạy từ JAR, lấy thư mục hiện tại
                return Paths.get("src/main/resources/icons");
            }
        } catch (Exception e) {
            // Ignore
        }

        // Fallback: thư mục hiện tại
        return Paths.get("src/main/resources/icons");
    }

    /**
     * Resize ảnh với kích thước tùy chỉnh
     */
    public void resizeImage(String sourcePath, String outputPath, int width, int height) throws IOException {
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            throw new IOException("Không tìm thấy file nguồn: " + sourcePath);
        }

        BufferedImage sourceImage = ImageIO.read(sourceFile);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(sourceImage, 0, 0, width, height, null);
        g2d.dispose();

        File outputFile = new File(outputPath);
        ImageIO.write(resized, "PNG", outputFile);
        System.out.println("Đã resize ảnh thành công: " + outputPath);
    }
}
