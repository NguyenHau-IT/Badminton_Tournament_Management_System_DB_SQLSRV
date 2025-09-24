package com.example.btms.model.match;

import javax.swing.JComponent;
import javax.swing.JFrame;
import java.util.Random;

public class CourtSession {
  public final String courtId; // ví dụ: "Sàn 1"
  public String header; // nội dung (MS/WS/…)
  public final BadmintonMatch match;
  public JFrame display; // cửa sổ bảng điểm (nếu đang mở)
  public JComponent controlPanel; // panel điều khiển trận đấu
  public boolean horizontal; // true = bảng điểm ngang
  public int screenIndex; // màn hình đã chọn
  public final String pinCode; // Mã PIN riêng cho mỗi sân

  public CourtSession(String courtId, String header, BadmintonMatch match) {
    this.courtId = courtId;
    this.header = header;
    this.match = match;
    this.pinCode = generatePinCode(); // Tạo mã PIN tự động
  }

  /**
   * Tạo mã PIN 4 chữ số cho sân
   */
  private String generatePinCode() {
    Random random = new Random();
    int pin = 1000 + random.nextInt(9000); // Tạo số từ 1000-9999
    return String.valueOf(pin);
  }

  /**
   * Lấy mã PIN của sân
   */
  public String getPinCode() {
    return pinCode;
  }

  @Override
  public String toString() {
    return "[" + courtId + "] " + (header == null || header.isBlank() ? "TRẬN ĐẤU" : header)
        + (display == null ? "  • (đang tắt)" : "");
  }
}
