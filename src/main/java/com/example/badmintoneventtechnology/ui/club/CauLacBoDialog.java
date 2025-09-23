package com.example.badmintoneventtechnology.ui.club;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.example.badmintoneventtechnology.model.club.CauLacBo;
import com.example.badmintoneventtechnology.service.club.CauLacBoService;

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
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        form.add(new JLabel("Tên CLB:"));
        form.add(tenField);
        form.add(new JLabel("Tên ngắn:"));
        form.add(tenNganField);
        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        buttons.add(btnSave);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        if (editMode) {
            tenField.setText(original.getTenClb());
            tenNganField.setText(original.getTenNgan());
        }

        pack();
        setLocationRelativeTo(parent);
    }

    private void onSave() {
        String ten = tenField.getText() != null ? tenField.getText().trim() : "";
        String tenNgan = tenNganField.getText() != null ? tenNganField.getText().trim() : null;
        try {
            if (editMode) {
                service.update(original.getId(), ten, tenNgan);
            } else {
                service.create(ten, tenNgan);
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
