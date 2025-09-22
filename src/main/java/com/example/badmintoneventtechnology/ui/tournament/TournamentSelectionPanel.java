package com.example.badmintoneventtechnology.ui.tournament;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import com.example.badmintoneventtechnology.model.tournament.Tournament;
import com.example.badmintoneventtechnology.config.Prefs;
import com.example.badmintoneventtechnology.service.match.CourtManagerService;
import com.example.badmintoneventtechnology.service.db.TournamentRepository;

/**
 * Panel để chọn giải đấu từ database
 */
public class TournamentSelectionPanel extends JPanel {
    private final TournamentRepository tournamentRepository;
    private JComboBox<Tournament> tournamentComboBox;
    private JLabel statusLabel;
    private TournamentSelectionListener selectionListener;
    private final Prefs prefs = new Prefs();

    public interface TournamentSelectionListener {
        void onTournamentSelected(Tournament tournament);
    }

    public TournamentSelectionPanel(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
        initComponents();
        loadTournaments();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Chọn Giải Đấu"));

        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));

        // Label hướng dẫn

        // ComboBox để chọn giải đấu
        tournamentComboBox = new JComboBox<>();
        tournamentComboBox.setPreferredSize(new Dimension(600, 30));
        tournamentComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Tournament selected = (Tournament) tournamentComboBox.getSelectedItem();
                if (selected != null) {
                    int saved = prefs.getInt("selectedTournamentVernr", -1);

                    // Nếu muốn đổi giải khác: chỉ cần không có SÂN đang mở (kể cả không thi đấu)
                    if (saved != -1 && selected.getVernr() != saved) {
                        boolean hasAnyCourtSession = !CourtManagerService.getInstance().getAllCourtIds().isEmpty();
                        boolean hasOpenDisplayOrControl = CourtManagerService.getInstance().getAllCourtStatus().values()
                                .stream().anyMatch(st -> st.isDisplayOpen);
                        if (hasAnyCourtSession || hasOpenDisplayOrControl) {
                            JOptionPane.showMessageDialog(
                                    TournamentSelectionPanel.this,
                                    "Không thể đổi giải đấu khi còn sân đang mở.\nVui lòng đóng tất cả sân (Display/Control), sau đó chọn lại.",
                                    "Không thể đổi giải đấu",
                                    JOptionPane.WARNING_MESSAGE);
                            // Khôi phục lại lựa chọn cũ
                            setSelectedTournament(saved);
                            return;
                        }
                    }

                    // Lưu lại ID giải đấu mới
                    if (selected.getVernr() != -1)
                        prefs.putInt("selectedTournamentVernr", selected.getVernr());
                    else
                        prefs.remove("selectedTournamentVernr");
                    if (selectionListener != null) {
                        selectionListener.onTournamentSelected(selected);
                    }
                }
            }
        });

        // Nút refresh
        JButton refreshButton = new JButton("Làm mới");
        refreshButton.addActionListener(e -> loadTournaments());

        // Panel trên
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(tournamentComboBox);
        topPanel.add(refreshButton);

        // Label trạng thái
        statusLabel = new JLabel("Sẵn sàng");
        statusLabel.setForeground(Color.BLUE);

        // Panel dưới
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(statusLabel);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Tải danh sách giải đấu từ database
     */
    public void loadTournaments() {
        statusLabel.setText("Đang tải...");
        statusLabel.setForeground(Color.ORANGE);

        SwingUtilities.invokeLater(() -> {
            try {
                List<Tournament> tournaments = tournamentRepository.loadAllTournaments();

                tournamentComboBox.removeAllItems();

                if (tournaments.isEmpty()) {
                    tournamentComboBox.addItem(new Tournament(-1, "-- Không có giải đấu --"));
                    statusLabel.setText("Không có giải đấu nào");
                    statusLabel.setForeground(Color.RED);
                } else {
                    for (Tournament tournament : tournaments) {
                        tournamentComboBox.addItem(tournament);
                    }
                    // Khôi phục lựa chọn trước đó nếu có
                    int savedVernr = prefs.getInt("selectedTournamentVernr", -1);
                    if (savedVernr != -1) {
                        setSelectedTournament(savedVernr);
                        // Gọi listener để các phần khác có thể cập nhật theo lựa chọn khôi phục
                        Tournament restored = getSelectedTournament();
                        if (restored != null && selectionListener != null) {
                            selectionListener.onTournamentSelected(restored);
                        }
                    }

                    statusLabel.setText("Đã tải " + tournaments.size() + " giải đấu");
                    statusLabel.setForeground(Color.GREEN);
                }
            } catch (Exception ex) {
                tournamentComboBox.removeAllItems();
                tournamentComboBox.addItem(new Tournament(-1, "-- Lỗi khi tải dữ liệu --"));
                statusLabel.setText("Lỗi: " + ex.getMessage());
                statusLabel.setForeground(Color.RED);
            }
        });
    }

    /**
     * Lấy giải đấu hiện tại được chọn
     */
    public Tournament getSelectedTournament() {
        Tournament selected = (Tournament) tournamentComboBox.getSelectedItem();
        return (selected != null && selected.getVernr() != -1) ? selected : null;
    }

    /**
     * Chọn giải đấu theo ID
     */
    public void setSelectedTournament(int vernr) {
        for (int i = 0; i < tournamentComboBox.getItemCount(); i++) {
            Tournament tournament = tournamentComboBox.getItemAt(i);
            if (tournament.getVernr() == vernr) {
                tournamentComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Thiết lập listener cho sự kiện chọn giải đấu
     */
    public void setSelectionListener(TournamentSelectionListener listener) {
        this.selectionListener = listener;
    }

    /**
     * Kiểm tra xem có giải đấu nào được chọn không
     */
    public boolean hasValidSelection() {
        return getSelectedTournament() != null;
    }

    /**
     * Làm mới danh sách giải đấu
     */
    public void refresh() {
        loadTournaments();
    }

    /** Giữ API cũ: hiện tại không cần khóa vĩnh viễn nữa. */
    public void lockSelection() {
        /* no-op */ }

    /** Giữ API cũ: mở khóa là no-op vì luôn cho phép đổi khi không có trận. */
    public void unlockSelection() {
        /* no-op */ }
}
