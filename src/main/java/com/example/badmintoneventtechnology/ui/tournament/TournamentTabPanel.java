package com.example.badmintoneventtechnology.ui.tournament;

import java.awt.BorderLayout;
import java.sql.Connection;

import javax.swing.JPanel;

import com.example.badmintoneventtechnology.model.tournament.GiaiDau;
import com.example.badmintoneventtechnology.service.db.DatabaseService;
import com.example.badmintoneventtechnology.service.tournament.GiaiDauService;

/**
 * Tab panel chính cho chức năng quản lý giải đấu
 * Sử dụng GiaiDau entity thay vì Tournament
 */
public class TournamentTabPanel extends JPanel {
    private final DatabaseService databaseService;
    private GiaiDauService giaiDauService;
    private GiaiDauManagementPanel giaiDauManagementPanel;

    public TournamentTabPanel(DatabaseService databaseService) {
        super(new BorderLayout(10, 10));
        this.databaseService = databaseService;

        // Lắng nghe thay đổi kết nối database
        updateConnection();
    }

    /**
     * Cập nhật kết nối database
     */
    public void updateConnection() {
        Connection connection = databaseService.current();
        if (connection != null) {
            giaiDauService = new GiaiDauService(databaseService);
            initComponents();
        } else {
            giaiDauService = null;
            initComponents();
        }
    }

    private void initComponents() {
        // Clear existing components
        removeAll();

        if (giaiDauService != null) {
            // Tạo panel quản lý giải đấu
            giaiDauManagementPanel = new GiaiDauManagementPanel(giaiDauService);
            add(giaiDauManagementPanel, BorderLayout.CENTER);
        } else {
            // Hiển thị thông báo không có kết nối
            JPanel noConnectionPanel = new JPanel();
            noConnectionPanel.add(new javax.swing.JLabel("Chưa có kết nối database. Vui lòng kết nối database trước."));
            add(noConnectionPanel, BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    /**
     * Lấy giải đấu hiện tại được chọn (nếu có)
     */
    public GiaiDau getSelectedGiaiDau() {
        if (giaiDauManagementPanel != null) {
            // Lấy giải đấu được chọn từ table (nếu có selection)
            return giaiDauManagementPanel.getSelectedGiaiDau();
        }
        return null;
    }

    /**
     * Làm mới danh sách giải đấu
     */
    public void refreshGiaiDau() {
        if (giaiDauManagementPanel != null) {
            giaiDauManagementPanel.refreshData();
        }
    }

    /**
     * Mở khóa cho phép chọn lại giải đấu (gọi khi Logout)
     */
    public void unlockSelection() {
        // Không cần thiết với GiaiDau vì luôn cho phép chọn
    }

    /**
     * Kiểm tra xem có giải đấu nào được chọn không
     */
    public boolean hasValidSelection() {
        return getSelectedGiaiDau() != null;
    }

    /**
     * Lấy service để sử dụng ở nơi khác
     */
    public GiaiDauService getGiaiDauService() {
        return giaiDauService;
    }
}