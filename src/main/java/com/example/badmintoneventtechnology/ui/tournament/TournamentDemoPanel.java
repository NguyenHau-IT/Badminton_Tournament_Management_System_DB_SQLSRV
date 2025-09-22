package com.example.badmintoneventtechnology.ui.tournament;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.example.badmintoneventtechnology.model.tournament.Tournament;
import com.example.badmintoneventtechnology.service.db.DatabaseService;
import com.example.badmintoneventtechnology.service.db.TournamentRepository;

/**
 * Demo panel để test TournamentSelectionPanel
 */
public class TournamentDemoPanel extends JPanel {
    private TournamentSelectionPanel tournamentPanel;
    private JTextArea infoTextArea;

    public TournamentDemoPanel(DatabaseService databaseService) {
        initComponents(databaseService);
    }

    private void initComponents(DatabaseService databaseService) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Demo - Chọn Giải Đấu"));

        // Tạo TournamentRepository
        TournamentRepository tournamentRepository = new TournamentRepository(databaseService.current());

        // Tạo panel chọn giải đấu
        tournamentPanel = new TournamentSelectionPanel(tournamentRepository);
        tournamentPanel.setSelectionListener(new TournamentSelectionPanel.TournamentSelectionListener() {
            @Override
            public void onTournamentSelected(Tournament tournament) {
                updateInfoText(tournament);
            }
        });

        // Text area để hiển thị thông tin
        infoTextArea = new JTextArea(8, 40);
        infoTextArea.setEditable(false);
        infoTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        infoTextArea.setText("Chọn một giải đấu từ dropdown để xem thông tin...");

        // Panel điều khiển
        JPanel controlPanel = new JPanel(new FlowLayout());

        JButton getSelectedButton = new JButton("Lấy giải đấu đã chọn");
        getSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Tournament selected = tournamentPanel.getSelectedTournament();
                if (selected != null) {
                    JOptionPane.showMessageDialog(TournamentDemoPanel.this,
                            "Giải đấu đã chọn:\n" +
                                    "ID: " + selected.getVernr() + "\n" +
                                    "Tên: " + selected.getBezeichnung(),
                            "Thông tin giải đấu",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(TournamentDemoPanel.this,
                            "Chưa có giải đấu nào được chọn!",
                            "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        JButton refreshButton = new JButton("Làm mới danh sách");
        refreshButton.addActionListener(e -> tournamentPanel.refresh());

        controlPanel.add(getSelectedButton);
        controlPanel.add(refreshButton);

        // Layout
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(tournamentPanel, BorderLayout.NORTH);
        leftPanel.add(controlPanel, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(infoTextArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Thông tin giải đấu"));

        add(leftPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void updateInfoText(Tournament tournament) {
        if (tournament.getVernr() == -1) {
            infoTextArea.setText("Không có giải đấu hợp lệ được chọn.");
            return;
        }

        StringBuilder info = new StringBuilder();
        info.append("=== THÔNG TIN GIẢI ĐẤU ===\n\n");
        info.append("ID giải đấu (VERNR): ").append(tournament.getVernr()).append("\n");
        info.append("Tên giải đấu: ").append(tournament.getBezeichnung()).append("\n\n");
        info.append("=== HƯỚNG DẪN SỬ DỤNG ===\n\n");
        info.append("1. Chọn giải đấu từ dropdown phía trên\n");
        info.append("2. Thông tin sẽ được cập nhật tự động\n");
        info.append("3. Sử dụng nút 'Lấy giải đấu đã chọn' để test\n");
        info.append("4. Sử dụng nút 'Làm mới' để tải lại danh sách\n\n");
        info.append("=== CODE SỬ DỤNG ===\n\n");
        info.append("// Lấy giải đấu đã chọn\n");
        info.append("Tournament selected = tournamentPanel.getSelectedTournament();\n");
        info.append("if (selected != null) {\n");
        info.append("    int id = selected.getVernr();\n");
        info.append("    String name = selected.getBezeichnung();\n");
        info.append("    // Xử lý logic với giải đấu đã chọn\n");
        info.append("}\n");

        infoTextArea.setText(info.toString());
    }
}
