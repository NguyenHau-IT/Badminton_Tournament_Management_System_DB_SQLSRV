package com.example.badmintoneventtechnology.ui.tournament;

import java.awt.BorderLayout;
import java.sql.Connection;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.example.badmintoneventtechnology.model.tournament.Tournament;
import com.example.badmintoneventtechnology.service.db.DatabaseService;
import com.example.badmintoneventtechnology.service.db.TournamentRepository;

/**
 * Tab panel chính cho chức năng quản lý giải đấu
 */
public class TournamentTabPanel extends JPanel {
    private final DatabaseService databaseService;
    private TournamentRepository tournamentRepository;
    private TournamentSelectionPanel tournamentSelectionPanel;
    private JTextArea infoTextArea;

    public TournamentTabPanel(DatabaseService databaseService) {
        super(new BorderLayout(10, 10));
        this.databaseService = databaseService;

        setBorder(new EmptyBorder(10, 10, 10, 10));
        initComponents();

        // Lắng nghe thay đổi kết nối database
        updateConnection();
    }

    private void initComponents() {
        // Panel chính chứa tất cả các thành phần
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        // Panel chọn giải đấu ở phía trên
        JPanel tournamentPanel = createTournamentSelectionPanel();
        mainPanel.add(tournamentPanel, BorderLayout.NORTH);

        // Panel thông tin ở phía dưới
        JPanel infoPanel = createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createTournamentSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Tạo TournamentRepository và SelectionPanel
        if (tournamentRepository != null) {
            tournamentSelectionPanel = new TournamentSelectionPanel(tournamentRepository);
            tournamentSelectionPanel.setSelectionListener(new TournamentSelectionPanel.TournamentSelectionListener() {
                @Override
                public void onTournamentSelected(Tournament tournament) {
                    updateInfoText(tournament);
                }
            });
            panel.add(tournamentSelectionPanel, BorderLayout.CENTER);
        } else {
            // Hiển thị thông báo lỗi nếu chưa có kết nối database
            JTextArea errorText = new JTextArea("Chưa có kết nối database. Vui lòng kết nối database trước.");
            errorText.setEditable(false);
            errorText.setOpaque(false);
            errorText.setBorder(new EmptyBorder(20, 20, 20, 20));
            panel.add(errorText, BorderLayout.CENTER);
        }

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Thông tin Giải Đấu",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        infoTextArea = new JTextArea(15, 50);
        infoTextArea.setEditable(false);
        infoTextArea.setFont(infoTextArea.getFont().deriveFont(12f));
        infoTextArea.setText(getInitialInfoText());

        JScrollPane scrollPane = new JScrollPane(infoTextArea);
        scrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private String getInitialInfoText() {
        return "=== THÔNG TIN GIẢI ĐẤU ===\n\n" +
                "Chọn một giải đấu từ dropdown phía trên để xem thông tin chi tiết.\n\n" +
                "=== HƯỚNG DẪN SỬ DỤNG ===\n\n" +
                "1. Đảm bảo đã kết nối database\n" +
                "2. Chọn giải đấu từ dropdown 'Chọn Giải Đấu'\n" +
                "3. Thông tin giải đấu sẽ được hiển thị ở đây\n" +
                "4. Sử dụng nút 'Làm mới' để tải lại danh sách\n\n" +
                "=== CẤU TRÚC DATABASE ===\n\n" +
                "Bảng VERANSTALTUNG:\n" +
                "- VERNR (INT): ID giải đấu\n" +
                "- BEZEICHNUNG (VARCHAR): Tên giải đấu\n\n" +
                "=== CODE SỬ DỤNG ===\n\n" +
                "// Lấy giải đấu đã chọn\n" +
                "Tournament selected = tournamentPanel.getSelectedTournament();\n" +
                "if (selected != null) {\n" +
                "    int id = selected.getVernr();\n" +
                "    String name = selected.getBezeichnung();\n" +
                "    // Xử lý logic với giải đấu đã chọn\n" +
                "}";
    }

    private void updateInfoText(Tournament tournament) {
        if (tournament == null || tournament.getVernr() == -1) {
            infoTextArea.setText(getInitialInfoText());
            return;
        }

        StringBuilder info = new StringBuilder();
        info.append("=== THÔNG TIN GIẢI ĐẤU ===\n\n");
        info.append("ID giải đấu: ").append(tournament.getVernr()).append("\n");
        info.append("Tên giải đấu: ").append(tournament.getBezeichnung()).append("\n\n");

        info.append("=== TRẠNG THÁI ===\n\n");
        info.append("✅ Giải đấu đã được chọn\n");
        info.append("✅ Sẵn sàng để sử dụng trong các chức năng khác\n\n");

        infoTextArea.setText(info.toString());
    }

    /**
     * Cập nhật kết nối database
     */
    public void updateConnection() {
        Connection connection = databaseService.current();
        if (connection != null) {
            tournamentRepository = new TournamentRepository(connection);
            // Tạo lại tournament selection panel với repository mới
            initComponents();
        } else {
            tournamentRepository = null;
            initComponents();
        }
    }

    /**
     * Lấy giải đấu hiện tại được chọn
     */
    public Tournament getSelectedTournament() {
        if (tournamentSelectionPanel != null) {
            return tournamentSelectionPanel.getSelectedTournament();
        }
        return null;
    }

    /**
     * Làm mới danh sách giải đấu
     */
    public void refreshTournaments() {
        if (tournamentSelectionPanel != null) {
            tournamentSelectionPanel.refresh();
        }
    }

    /** Mở khóa cho phép chọn lại giải đấu (gọi khi Logout). */
    public void unlockSelection() {
        if (tournamentSelectionPanel != null) {
            tournamentSelectionPanel.unlockSelection();
        }
    }

    /**
     * Kiểm tra xem có giải đấu nào được chọn không
     */
    public boolean hasValidSelection() {
        return tournamentSelectionPanel != null && tournamentSelectionPanel.hasValidSelection();
    }
}
