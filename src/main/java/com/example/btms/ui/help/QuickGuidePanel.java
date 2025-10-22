package com.example.btms.ui.help;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Trang hướng dẫn nhanh (Quick Guide) cho người dùng.
 */
public class QuickGuidePanel extends JPanel {

    private final JEditorPane html = new JEditorPane();

    public QuickGuidePanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        html.setContentType("text/html; charset=UTF-8");
        html.setEditable(false);
        html.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        html.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        html.setFont(html.getFont().deriveFont(Font.PLAIN, 14f));

        html.setText(buildContent());

        JScrollPane sp = new JScrollPane(html);
        sp.setBorder(BorderFactory.createEmptyBorder());
        add(sp, BorderLayout.CENTER);
    }

    private String buildContent() {
        // Minimal inline styles for readability
        String css = "<style>\n"
                + "body{font-family:Segoe UI,Arial,sans-serif;font-size:14px;color:#222;}\n"
                + "h1{font-size:20px;margin:0 0 8px;}\n"
                + "h2{font-size:16px;margin:18px 0 6px;}\n"
                + "ul{margin:6px 0 12px 20px;}\n"
                + "li{margin:4px 0;}\n"
                + ".note{color:#666;}\n"
                + ".kbd{background:#f2f2f2;border:1px solid #ddd;padding:0 4px;border-radius:3px;}\n"
                + "</style>";

        String body = "<h1>Hướng dẫn nhanh</h1>"
                + "<div class='note'>Trang này tóm tắt các tính năng chính theo từng chế độ: ADMINISTRATOR và MATCH.</div>"

                + "<h2>Chung</h2>"
                + "<ul>"
                + "<li>Vào <b>Hệ thống &rarr; Đăng nhập</b> để đăng nhập và chọn <b>Chế độ</b> (ADMINISTRATOR/MATCH).</li>"
                + "<li>Sau khi <b>kết nối CSDL</b> và <b>đăng nhập</b>, dùng <b>Quản lý &rarr; Chọn giải đấu...</b> để chọn giải.</li>"
                + "<li>Có thể đổi giao diện, phóng to chữ tại <b>Khác &rarr; Cài đặt</b>.</li>"
                + "</ul>"

                + "<h2>Chế độ ADMINISTRATOR</h2>"
                + "<ul>"
                + "<li><b>Giải đấu</b>: tạo/sửa thông tin giải, ngày thi đấu, sân, trọng tài...</li>"
                + "<li><b>Nội dung</b>: quản lý các nội dung thi đấu (đơn/đôi, lứa tuổi, hạng...)</li>"
                + "<li><b>Câu lạc bộ</b> &amp; <b>Vận động viên</b>: quản lý CLB và hồ sơ VĐV.</li>"
                + "<li><b>Nội dung của giải</b>: gán nội dung vào giải, cấu hình hạng mục thi.</li>"
                + "<li><b>Đăng ký đội / cá nhân</b>: nhập danh sách tham dự cho nội dung.</li>"
                + "<li><b>Danh sách đăng kí</b>: xem nhanh VĐV/Đội theo từng nội dung.</li>"
                + "<li><b>Bốc thăm thi đấu</b>: bốc thăm và sinh <b>Sơ đồ thi đấu</b>.</li>"
                + "<li><b>Thi đấu</b>: điều khiển nhiều sân, cập nhật điểm trực tiếp.</li>"
                + "<li><b>Giám sát</b>: mở các bảng điện tử theo dõi trận đấu.</li>"
                + "<li><b>Kết quả đã thi đấu</b>: xem tổng hợp các trận đã hoàn thành.</li>"
                + "<li><b>Trang biên bản</b>: xem biên bản chi tiết theo set/điểm.</li>"
                + "<li><b>Tổng sắp huy chương</b>: tổng hợp huy chương theo CLB.</li>"
                + "<li><b>Báo cáo (PDF)</b>: xuất các báo cáo/tổng hợp ra PDF.</li>"
                + "<li><b>Khác</b>: <i>Logs</i>, <i>System Logs</i>, <i>Sao lưu CSDL...</i></li>"
                + "</ul>"

                + "<h2>Chế độ MATCH</h2>"
                + "<ul>"
                + "<li><b>Giám sát</b>: hiển thị các màn hình theo dõi trận đấu.</li>"
                + "<li><b>Sơ đồ thi đấu</b>: xem sơ đồ cho các nội dung đã bốc thăm.</li>"
                + "<li><b>Kết quả đã thi đấu</b>: theo dõi kết quả các trận đã kết thúc.</li>"
                + "<li><b>Tổng sắp huy chương</b> &amp; <b>Báo cáo (PDF)</b>: xem/tải tổng hợp.</li>"
                + "</ul>"

                + "<h2>Mẹo sử dụng nhanh</h2>"
                + "<ul>"
                + "<li>Nếu không thấy mục cần thiết trong menu, hãy <b>đăng nhập</b> và kiểm tra đúng <b>Chế độ</b>.</li>"
                + "<li>Nếu vừa thay đổi giải đấu, dùng mục <b>Hệ thống &rarr; Làm mới dữ liệu</b> để cập nhật.</li>"
                + "<li>Click phải vào <b>Cây điều hướng</b> bên trái để mở <i>menu ngữ cảnh</i> của nội dung.</li>"
                + "</ul>";

        return "<html><head>" + css + "</head><body>" + body + "</body></html>";
    }
}
