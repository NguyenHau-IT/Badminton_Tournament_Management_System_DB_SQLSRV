package com.example.btms.ui.club;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.example.btms.model.club.CauLacBo;
import com.example.btms.service.club.CauLacBoService;

public class CauLacBoDialog extends JDialog {
    private final CauLacBoService service;
    private final CauLacBo original;
    private final boolean editMode;

    private final JTextField tenField = new JTextField(28);
    private final JTextField tenNganField = new JTextField(18);

    public CauLacBoDialog(Window parent, String title, CauLacBo clb, CauLacBoService service) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.service = service;
        this.original = clb;
        this.editMode = clb != null;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Thông tin Câu lạc bộ"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.anchor = GridBagConstraints.WEST;

        // Tên CLB
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Tên CLB:"), gc);
        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        tenField.setToolTipText("Tên đầy đủ của câu lạc bộ (bắt buộc)");
        form.add(tenField, gc);

        // Tên ngắn
        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Tên ngắn:"), gc);
        gc.gridx = 1;
        gc.gridy = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        tenNganField.setToolTipText("Tên rút gọn/viết tắt (tối đa 100 ký tự, có thể để trống)");
        form.add(tenNganField, gc);

        wrap.add(form, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        buttons.add(btnSave);
        buttons.add(btnCancel);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        bottom.add(buttons, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        if (editMode) {
            tenField.setText(original.getTenClb());
            tenNganField.setText(original.getTenNgan());
        }

        // Phím tắt và default button
        getRootPane().setDefaultButton(btnSave);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(parent);
    }

    private void onSave() {
        String ten = tenField.getText() != null ? tenField.getText().trim() : "";
        String tenNgan = tenNganField.getText() != null ? tenNganField.getText().trim() : null;

        // Tiền kiểm để báo lỗi thân thiện trước khi gọi service
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên CLB.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            tenField.requestFocus();
            return;
        }
        if (ten.length() > 255) {
            JOptionPane.showMessageDialog(this, "Tên CLB tối đa 255 ký tự.", "Sai dữ liệu",
                    JOptionPane.WARNING_MESSAGE);
            tenField.requestFocus();
            return;
        }
        if (tenNgan != null && !tenNgan.isEmpty() && tenNgan.length() > 100) {
            JOptionPane.showMessageDialog(this, "Tên ngắn tối đa 100 ký tự.", "Sai dữ liệu",
                    JOptionPane.WARNING_MESSAGE);
            tenNganField.requestFocus();
            return;
        }

        try {
            if (editMode) {
                service.update(original.getId(), ten, tenNgan);
            } else {
                service.create(ten, tenNgan);
            }
            dispose();
        } catch (IllegalArgumentException | IllegalStateException | java.util.NoSuchElementException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
