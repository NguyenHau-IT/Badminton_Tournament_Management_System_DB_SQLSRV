package com.example.badmintoneventtechnology.ui.tournament;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;

import com.example.badmintoneventtechnology.model.tournament.Giai;
import com.example.badmintoneventtechnology.model.tournament.SuKien;
import com.example.badmintoneventtechnology.service.db.DatabaseService;

/**
 * Tab quản lý nội dung thi đấu (SuKien) cho giải đang chọn
 */
public class SuKienTab extends JPanel {
    private final DatabaseService service;
    private DefaultTableModel model;
    private JTable table;
    private JButton btnAdd, btnEdit, btnDelete;
    private Giai selectedGiai;
    private final JLabel lblCurrentGiai = new JLabel("Chưa chọn giải");

    public SuKienTab(DatabaseService service) {
        this.service = service;
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        model = new DefaultTableModel(
                new Object[] { "ID", "Mã", "Tên", "Nhóm tuổi", "Trình độ", "Số lượng", "Luật", "Loại bảng" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Cập nhật trạng thái nút khi chọn dòng
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtons();
            }
        });
        // Double-click để mở sửa nhanh
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    editSuKien();
                }
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAdd = new JButton("Thêm");
        btnEdit = new JButton("Sửa");
        btnDelete = new JButton("Xoá");
        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        top.add(btnPanel, BorderLayout.WEST);
        top.add(lblCurrentGiai, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        btnAdd.addActionListener(e -> addSuKien());
        btnEdit.addActionListener(e -> editSuKien());
        btnDelete.addActionListener(e -> deleteSuKien());

        updateButtons();
    }

    private void updateButtons() {
        boolean enabled = selectedGiai != null && service.isConnected();
        btnAdd.setEnabled(enabled);
        btnEdit.setEnabled(enabled && table.getSelectedRow() != -1);
        btnDelete.setEnabled(enabled && table.getSelectedRow() != -1);
    }

    public void setSelectedGiai(Giai g) {
        this.selectedGiai = g;
        lblCurrentGiai.setText(g == null ? "Chưa chọn giải" : ("Giải: " + g.getTen()));
        loadData();
        updateButtons();
    }

    private void loadData() {
        model.setRowCount(0);
        if (selectedGiai == null || !service.isConnected())
            return;
        List<SuKien> list = service.getSuKienByGiai(selectedGiai.getGiaiId());
        for (SuKien s : list) {
            model.addRow(new Object[] { s.getSuKienId(), s.getMa(), s.getTen(), s.getNhomTuoi(), s.getTrinhDo(),
                    s.getSoLuong(), s.getLuatThiDau(), s.getLoaiBang() });
        }
        // Sau khi load lại dữ liệu thì reset chọn & nút
        if (table.getRowCount() > 0) {
            table.clearSelection();
        }
        updateButtons();
    }

    private void addSuKien() {
        if (selectedGiai == null)
            return;
        SuKienFormDialog dlg = new SuKienFormDialog(null);
        if (dlg.showDialog()) {
            SuKien s = dlg.getSuKienResult(selectedGiai.getGiaiId());
            try {
                service.addSuKien(s);
                loadData();
            } catch (RuntimeException ex) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Không thêm được sự kiện:\n" + ex.getMessage()
                                + "\nGợi ý: kiểm tra trùng (mã, nhóm tuổi, trình độ) hoặc mã không hợp lệ.",
                        "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSuKien() {
        int row = table.getSelectedRow();
        if (row == -1 || selectedGiai == null)
            return;
        int id = (int) model.getValueAt(row, 0);
        // Tạo đối tượng SuKien từ hàng hiện tại (vì chưa có getSuKienById)
        SuKien s = new SuKien();
        s.setSuKienId(id);
        s.setGiaiId(selectedGiai.getGiaiId());
        s.setMa((String) model.getValueAt(row, 1));
        s.setTen((String) model.getValueAt(row, 2));
        s.setNhomTuoi((String) model.getValueAt(row, 3));
        s.setTrinhDo((String) model.getValueAt(row, 4));
        s.setSoLuong((int) model.getValueAt(row, 5));
        s.setLuatThiDau((String) model.getValueAt(row, 6));
        s.setLoaiBang((String) model.getValueAt(row, 7));

        SuKienFormDialog dlg = new SuKienFormDialog(s);
        if (dlg.showDialog()) {
            SuKien updated = dlg.getSuKienResult(selectedGiai.getGiaiId());
            updated.setSuKienId(id);
            service.updateSuKien(updated);
            loadData();
        }
    }

    private void deleteSuKien() {
        int row = table.getSelectedRow();
        if (row == -1 || selectedGiai == null)
            return;
        int id = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Xoá nội dung này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.deleteSuKien(id);
            loadData();
        }
    }
}
