package com.example.btms.ui.player;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.example.btms.model.category.NoiDung;
import com.example.btms.model.player.VanDongVien;
import com.example.btms.service.player.DangKyCaNhanService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.model.player.DangKyCaNhan;

/** Dialog thêm / sửa đăng ký cá nhân */
public class DangKyCaNhanDialog extends JDialog {
    private final DangKyCaNhanService dkService;
    private final VanDongVienService vdvService;
    private final NoiDungService noiDungService;
    private final int idGiai;
    private DangKyCaNhan editing;

    private final JComboBox<NoiDung> cboNoiDung = new JComboBox<>();
    private final JComboBox<VanDongVien> cboVdv = new JComboBox<>();

    public DangKyCaNhanDialog(Window owner, String title, int idGiai, DangKyCaNhanService dkService,
            VanDongVienService vdvService, NoiDungService ndService, DangKyCaNhan editing) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.idGiai = idGiai;
        this.dkService = dkService;
        this.vdvService = vdvService;
        this.noiDungService = ndService;
        this.editing = editing;
        buildUI();
        loadData();
        if (editing != null) prefill();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel form = new JPanel(new java.awt.GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Nội dung (đơn):"));
        form.add(cboNoiDung);
        form.add(new JLabel("Vận động viên:"));
        form.add(cboVdv);

        JButton btnOk = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        btnOk.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnOk); buttons.add(btnCancel);

        getContentPane().setLayout(new BorderLayout(10,10));
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    private void loadData() {
        try {
            // Nội dung đơn (TEAM = false)
            DefaultComboBoxModel<NoiDung> ndModel = new DefaultComboBoxModel<>();
            for (NoiDung nd : noiDungService.getAllNoiDung()) {
                if (!Boolean.TRUE.equals(nd.getTeam())) ndModel.addElement(nd);
            }
            cboNoiDung.setModel(ndModel);

            DefaultComboBoxModel<VanDongVien> vdvModel = new DefaultComboBoxModel<>();
            for (VanDongVien v : vdvService.findAll()) vdvModel.addElement(v);
            cboVdv.setModel(vdvModel);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void prefill() {
        // Chọn lại combobox dựa trên editing
        for (int i=0;i<cboNoiDung.getItemCount();i++) {
            if (cboNoiDung.getItemAt(i).getId().equals(editing.getIdNoiDung())) { cboNoiDung.setSelectedIndex(i); break; }
        }
        for (int i=0;i<cboVdv.getItemCount();i++) {
            if (cboVdv.getItemAt(i).getId().equals(editing.getIdVdv())) { cboVdv.setSelectedIndex(i); break; }
        }
    }

    private void onSave() {
        NoiDung nd = (NoiDung) cboNoiDung.getSelectedItem();
        VanDongVien v = (VanDongVien) cboVdv.getSelectedItem();
        if (nd == null || v == null) {
            JOptionPane.showMessageDialog(this, "Chọn đầy đủ nội dung và VĐV");
            return;
        }
        try {
            if (editing == null) {
                dkService.create(idGiai, nd.getId(), v.getId());
            } else {
                dkService.update(editing.getId(), idGiai, nd.getId(), v.getId());
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
