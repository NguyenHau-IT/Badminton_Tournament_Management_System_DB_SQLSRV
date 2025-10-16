package com.example.btms.ui.tournament;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.example.btms.config.Prefs;
import com.example.btms.model.tournament.GiaiDau;
import com.example.btms.service.match.CourtManagerService;
import com.example.btms.service.tournament.GiaiDauService;

/**
 * Panel cho phép chọn hoặc tạo mới giải đấu sau khi đăng nhập
 */
public class GiaiDauSelectPanel extends JPanel {
    private final GiaiDauService giaiDauService;
    private JComboBox<GiaiDau> comboGiaiDau;
    private JButton btnCreateNew;
    private JButton btnSelect;
    private GiaiDau selectedGiaiDau;
    private boolean selectionLocked = false; // khóa sau khi chọn

    public GiaiDauSelectPanel(GiaiDauService giaiDauService) {
        this.giaiDauService = giaiDauService;
        setLayout(new BorderLayout(10, 10));
        initComponents();
        loadGiaiDauList();
    }

    private void initComponents() {
        comboGiaiDau = new JComboBox<>();
        comboGiaiDau.setPreferredSize(new Dimension(300, 28));
        comboGiaiDau.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof GiaiDau gd) {
                    setText(gd.getTenGiai());
                }
                return c;
            }
        });

        btnCreateNew = new JButton("Tạo mới giải đấu");
        btnSelect = new JButton("Chọn giải đấu này");

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Chọn giải đấu để tổ chức thi đấu:"));
        topPanel.add(comboGiaiDau);
        topPanel.add(btnSelect);
        topPanel.add(btnCreateNew);
        add(topPanel, BorderLayout.NORTH);

        btnCreateNew.addActionListener(e -> showCreateDialog());
        btnSelect.addActionListener(e -> onSelectOrChange());
    }

    private void loadGiaiDauList() {
        comboGiaiDau.removeAllItems();
        Integer keepId = (selectedGiaiDau != null) ? selectedGiaiDau.getId() : null;
        try {
            List<GiaiDau> list = giaiDauService.getAllGiaiDau();
            for (GiaiDau gd : list) {
                comboGiaiDau.addItem(gd);
            }

            if (!list.isEmpty()) {
                // Nếu đã khóa và có lựa chọn trước đó thì giữ nguyên lựa chọn cũ nếu còn tồn
                // tại
                if (selectionLocked && keepId != null) {
                    int idx = -1;
                    for (int i = 0; i < comboGiaiDau.getItemCount(); i++) {
                        GiaiDau gd = comboGiaiDau.getItemAt(i);
                        if (gd != null) {
                            Integer id = gd.getId();
                            if (Objects.equals(id, keepId)) {
                                idx = i;
                                break;
                            }
                        }
                    }
                    if (idx >= 0) {
                        comboGiaiDau.setSelectedIndex(idx);
                        // không thay đổi selectedGiaiDau
                    } else {
                        comboGiaiDau.setSelectedIndex(0);
                        if (!selectionLocked)
                            selectedGiaiDau = list.get(0);
                    }
                } else {
                    // Chưa khóa: chọn phần tử đầu và cập nhật selection
                    comboGiaiDau.setSelectedIndex(0);
                    selectedGiaiDau = list.get(0);
                }
            }
        } catch (SQLException | RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách giải đấu: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCreateDialog() {
        GiaiDauDialog dialog = new GiaiDauDialog(SwingUtilities.getWindowAncestor(this), "Tạo mới giải đấu", null,
                giaiDauService);
        dialog.setVisible(true);
        loadGiaiDauList();
    }

    private boolean selectGiaiDau() {
        selectedGiaiDau = (GiaiDau) comboGiaiDau.getSelectedItem();
        if (selectedGiaiDau == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giải đấu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        Prefs prefs = new Prefs();
        prefs.putInt("selectedGiaiDauId", selectedGiaiDau.getId());
        prefs.put("selectedGiaiDauName", selectedGiaiDau.getTenGiai());
        return true;
    }

    private void onSelectOrChange() {
        if (!selectionLocked) {
            // Chế độ chọn -> xác nhận và khóa lại
            if (selectGiaiDau()) {
                lockUI();
            }
        } else {
            // Chế độ "Đổi giải" -> mở khóa để cho phép chọn lại
            if (canChangeTournamentNow()) {
                unlockUI();
            }
        }
    }

    private boolean canChangeTournamentNow() {
        // Không cho đổi nếu còn sân đang mở (display hoặc control còn tồn tại)
        var map = CourtManagerService.getInstance().getAllCourtStatus();
        boolean anyOpen = map.values().stream().anyMatch(s -> s != null && s.isDisplayOpen);
        if (anyOpen) {
            JOptionPane.showMessageDialog(this,
                    "Không thể đổi giải khi còn sân đang mở.\nVui lòng đóng tất cả sân trước khi đổi giải.",
                    "Đổi giải bị chặn", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void lockUI() {
        selectionLocked = true;
        comboGiaiDau.setEnabled(false);
        btnSelect.setText("Đổi giải");
    }

    private void unlockUI() {
        selectionLocked = false;
        comboGiaiDau.setEnabled(true);
        btnSelect.setText("Chọn giải đấu này");
    }

    public GiaiDau getSelectedGiaiDau() {
        return selectedGiaiDau;
    }
}
