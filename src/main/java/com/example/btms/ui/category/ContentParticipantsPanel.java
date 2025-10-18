package com.example.btms.ui.category;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.example.btms.model.category.NoiDung;
import java.awt.Cursor;

/**
 * Trang xem người tham gia theo nội dung:
 * - Nếu nội dung đơn: danh sách VĐV đăng ký.
 * - Nếu nội dung đôi: danh sách đội + ghép 2 thành viên.
 */
public class ContentParticipantsPanel extends JPanel {

    // ---- UI controls (expose qua getters cho Controller)
    private final JComboBox<NoiDung> cboNoiDung = new JComboBox<>();
    private final JLabel lblInfo = new JLabel("Chưa chọn nội dung");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Tên", "CLB", "Thành viên" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    private final JButton btnReload = new JButton("Tải lại");
    private final JTextField txtSearch = new JTextField(16);
    private final JButton btnEditTeam = new JButton("Sửa đội");
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnTransfer = new JButton("Chuyển nội dung");

    public ContentParticipantsPanel() {
        setLayout(new BorderLayout(8, 8));
        add(buildTop(), BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        line.add(new JLabel("Nội dung:"));
        cboNoiDung.setPrototypeDisplayValue(new NoiDung(0, "XXXXXXXXXXXXXXXXXXXXXXXX", null, null, null, false));
        line.add(cboNoiDung);
        line.add(btnReload);
        line.add(btnTransfer);
        line.add(btnEditTeam);
        line.add(btnDelete);
        line.add(new JLabel("Tìm:"));
        line.add(txtSearch);
        line.add(lblInfo);
        p.add(line, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        return p;
    }

    // ---- Helpers/UI state (controller có thể dùng)
    public void setBusy(boolean busy) {
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        setEnabled(!busy);
    }

    public void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // ---- Getters
    public JComboBox<NoiDung> getCboNoiDung() {
        return cboNoiDung;
    }

    public JLabel getLblInfo() {
        return lblInfo;
    }

    public DefaultTableModel getModel() {
        return model;
    }

    public JTable getTable() {
        return table;
    }

    public TableRowSorter<DefaultTableModel> getSorter() {
        return sorter;
    }

    public JButton getBtnReload() {
        return btnReload;
    }

    public JTextField getTxtSearch() {
        return txtSearch;
    }

    public JButton getBtnEditTeam() {
        return btnEditTeam;
    }

    public JButton getBtnDelete() {
        return btnDelete;
    }

    public JButton getBtnTransfer() {
        return btnTransfer;
    }
}