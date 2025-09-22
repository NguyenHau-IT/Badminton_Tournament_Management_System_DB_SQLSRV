package com.example.badmintoneventtechnology.ui.tournament;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.example.badmintoneventtechnology.config.Prefs;
import com.example.badmintoneventtechnology.model.tournament.GiaiDau;
import com.example.badmintoneventtechnology.service.tournament.GiaiDauService;

/**
 * Panel cho phép chọn hoặc tạo mới giải đấu sau khi đăng nhập
 */
public class GiaiDauSelectPanel extends JPanel {
    private final GiaiDauService giaiDauService;
    private JComboBox<GiaiDau> comboGiaiDau;
    private JButton btnCreateNew;
    private JButton btnSelect;
    private GiaiDau selectedGiaiDau;

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
                    setText(gd.getTenGiai() + " (ID: " + gd.getId() + ")");
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
        btnSelect.addActionListener(e -> selectGiaiDau());
    }

    private void loadGiaiDauList() {
        comboGiaiDau.removeAllItems();
        try {
            List<GiaiDau> list = giaiDauService.getAllGiaiDau();
            for (GiaiDau gd : list) {
                comboGiaiDau.addItem(gd);
            }
            if (!list.isEmpty()) {
                comboGiaiDau.setSelectedIndex(0);
                selectedGiaiDau = list.get(0);
            }
        } catch (Exception e) {
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

    private void selectGiaiDau() {
        selectedGiaiDau = (GiaiDau) comboGiaiDau.getSelectedItem();
        if (selectedGiaiDau == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giải đấu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Prefs prefs = new Prefs();
        prefs.putInt("selectedGiaiDauId", selectedGiaiDau.getId());
        JOptionPane.showMessageDialog(this, "Đã chọn giải đấu: " + selectedGiaiDau.getTenGiai(), "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public GiaiDau getSelectedGiaiDau() {
        return selectedGiaiDau;
    }
}
