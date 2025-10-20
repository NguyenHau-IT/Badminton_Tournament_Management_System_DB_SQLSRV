package com.example.btms.ui.category;

import com.example.btms.model.category.NoiDung;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * Dialog chuyển đăng ký giữa các nội dung theo giao diện 2 cột (trái -> phải).
 * Hỗ trợ cả chế độ CÁ NHÂN (singles) và ĐỘI (đôi).
 */
public class ChangeCategoryTransferDialog extends JDialog {
    // ---- UI widgets (public getters cho Controller dùng)
    private final JComboBox<NoiDung> cboLeft = new JComboBox<>();
    private final JComboBox<NoiDung> cboRight = new JComboBox<>();

    private final DefaultTableModel leftModel = new DefaultTableModel(
            new Object[] { "ID", "Tên", "CLB", "Thành viên" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel rightModel = new DefaultTableModel(
            new Object[] { "ID", "Tên", "CLB", "Thành viên" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    private final JTable tblLeft = new JTable(leftModel);
    private final JTable tblRight = new JTable(rightModel);

    private final JButton btnToRight = new JButton(">>");
    private final JButton btnRefresh = new JButton("Tải lại");
    private final JButton btnClose = new JButton("Đóng");

    public ChangeCategoryTransferDialog(Window owner, boolean teamMode, List<NoiDung> categories, NoiDung initialLeft) {
        super(owner, teamMode ? "Chuyển nội dung đội" : "Chuyển nội dung đơn", ModalityType.MODELESS);
        Objects.requireNonNull(categories, "categories");

        setLayout(new BorderLayout(10, 10));

        // Top
        JPanel top = new JPanel(new GridLayout(1, 2, 10, 0));
        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftTop.add(new JLabel("Nội dung trái:"));
        cboLeft.setPrototypeDisplayValue(new NoiDung(0, "XXXXXXXXXXXXXXXXXXXXXXXX", null, null, null, teamMode));
        leftTop.add(cboLeft);
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rightTop.add(new JLabel("Nội dung phải:"));
        cboRight.setPrototypeDisplayValue(new NoiDung(0, "XXXXXXXXXXXXXXXXXXXXXXXX", null, null, null, teamMode));
        rightTop.add(cboRight);
        top.add(leftTop);
        top.add(rightTop);
        add(top, BorderLayout.NORTH);

        // Center
        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = .5;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        center.add(new JScrollPane(tblLeft), gc);

        JPanel midButtons = new JPanel(new GridLayout(3, 1, 0, 8));
        midButtons.add(btnToRight);
        midButtons.add(btnRefresh);
        midButtons.add(btnClose);
        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.VERTICAL;
        center.add(midButtons, gc);

        gc.gridx = 2;
        gc.gridy = 0;
        gc.weightx = .5;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        center.add(new JScrollPane(tblRight), gc);

        add(center, BorderLayout.CENTER);

        // Bind combos
        DefaultComboBoxModel<NoiDung> leftModelCbo = new DefaultComboBoxModel<>(categories.toArray(NoiDung[]::new));
        DefaultComboBoxModel<NoiDung> rightModelCbo = new DefaultComboBoxModel<>(categories.toArray(NoiDung[]::new));
        cboLeft.setModel(leftModelCbo);
        cboRight.setModel(rightModelCbo);

        // select defaults
        if (initialLeft != null)
            selectCombo(cboLeft, initialLeft.getId());
        else if (cboLeft.getItemCount() > 0)
            cboLeft.setSelectedIndex(0);
        if (cboRight.getItemCount() > 0) {
            NoiDung left = (NoiDung) cboLeft.getSelectedItem();
            int idx = 0;
            if (left != null) {
                for (int i = 0; i < cboRight.getItemCount(); i++) {
                    NoiDung nd = cboRight.getItemAt(i);
                    if (nd != null && !nd.getId().equals(left.getId())) {
                        idx = i;
                        break;
                    }
                }
            }
            cboRight.setSelectedIndex(idx);
        }

        setSize(980, 520);
        setLocationRelativeTo(owner);
        setAlwaysOnTop(true);
    }

    private void selectCombo(JComboBox<NoiDung> combo, Integer id) {
        if (id == null)
            return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            NoiDung nd = combo.getItemAt(i);
            if (nd != null && id.equals(nd.getId())) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    // ----- Getters để Controller dùng -----
    public JComboBox<NoiDung> getCboLeft() {
        return cboLeft;
    }

    public JComboBox<NoiDung> getCboRight() {
        return cboRight;
    }

    public JTable getTblLeft() {
        return tblLeft;
    }

    public JTable getTblRight() {
        return tblRight;
    }

    public DefaultTableModel getLeftModel() {
        return leftModel;
    }

    public DefaultTableModel getRightModel() {
        return rightModel;
    }

    public JButton getBtnToRight() {
        return btnToRight;
    }

    public JButton getBtnRefresh() {
        return btnRefresh;
    }

    public JButton getBtnClose() {
        return btnClose;
    }

    // ---- Helpers chung cho Controller (không dính service) ----
    public void setBusy(boolean busy) {
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        getRootPane().setEnabled(!busy);
    }

    public void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
