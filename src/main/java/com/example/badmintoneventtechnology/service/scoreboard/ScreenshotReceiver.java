package com.example.badmintoneventtechnology.service.scoreboard;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

/**
 * Service để nhận screenshot từ các client
 */
public class ScreenshotReceiver {
    private DatagramSocket socket;
    private boolean running = false;
    private ExecutorService executor;
    private ScreenshotListener listener;

    public interface ScreenshotListener {
        void onScreenshotReceived(String fileName, String matchInfo, BufferedImage image, InetAddress clientAddress);
    }

    public ScreenshotReceiver(ScreenshotListener listener) {
        this.listener = listener;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void start() {
        if (running)
            return;

        try {
            socket = new DatagramSocket(2346); // Port để nhận screenshot
            running = true;

            executor.submit(() -> {
                while (running) {
                    try {
                        // Buffer để nhận dữ liệu
                        byte[] buffer = new byte[65507]; // Max UDP packet size
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                        socket.receive(packet);
                        processScreenshotPacket(packet);

                    } catch (Exception ex) {
                        if (running) {
                            System.err.println("Lỗi khi nhận screenshot: " + ex.getMessage());
                        }
                    }
                }
            });

            // ScreenshotReceiver đã khởi động thành công trên port 2346

        } catch (Exception ex) {
            System.err.println("Không thể khởi động ScreenshotReceiver: " + ex.getMessage());
        }
    }

    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

    private void processScreenshotPacket(DatagramPacket packet) {
        try {
            byte[] data = packet.getData();
            int length = packet.getLength();

            // Tìm vị trí dấu | đầu tiên để tách header
            int firstPipe = -1;
            for (int i = 0; i < length; i++) {
                if (data[i] == '|') {
                    firstPipe = i;
                    break;
                }
            }

            if (firstPipe == -1)
                return;

            // Đọc header
            String header = new String(data, 0, firstPipe, "UTF-8");
            if (!"SCREENSHOT".equals(header))
                return;

            // Tìm các dấu | tiếp theo
            int secondPipe = -1, thirdPipe = -1;
            for (int i = firstPipe + 1; i < length; i++) {
                if (data[i] == '|') {
                    if (secondPipe == -1) {
                        secondPipe = i;
                    } else {
                        thirdPipe = i;
                        break;
                    }
                }
            }

            if (secondPipe == -1 || thirdPipe == -1)
                return;

            // Parse thông tin từ header
            String fileName = new String(data, firstPipe + 1, secondPipe - firstPipe - 1, "UTF-8");
            String matchInfo = new String(data, secondPipe + 1, thirdPipe - secondPipe - 1, "UTF-8");

            // Đọc kích thước ảnh
            int imageSizeStart = thirdPipe + 1;
            int imageSizeEnd = -1;
            for (int i = imageSizeStart; i < length; i++) {
                if (data[i] == '|') {
                    imageSizeEnd = i;
                    break;
                }
            }

            if (imageSizeEnd == -1)
                return;

            String sizeStr = new String(data, imageSizeStart, imageSizeEnd - imageSizeStart, "UTF-8");
            int imageSize = Integer.parseInt(sizeStr);

            // Đọc dữ liệu ảnh
            int imageDataStart = imageSizeEnd + 1;
            if (imageDataStart + imageSize > length)
                return;

            byte[] imageData = new byte[imageSize];
            System.arraycopy(data, imageDataStart, imageData, 0, imageSize);

            // Chuyển byte array thành BufferedImage
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));

            if (image != null && listener != null) {
                listener.onScreenshotReceived(fileName, matchInfo, image, packet.getAddress());
            }

        } catch (Exception ex) {
            System.err.println("Lỗi khi xử lý screenshot packet: " + ex.getMessage());
        }
    }
}
