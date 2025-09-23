package com.example.badmintoneventtechnology.ui.player;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.example.badmintoneventtechnology.model.club.CauLacBo;
import com.example.badmintoneventtechnology.model.player.VanDongVien;
import com.example.badmintoneventtechnology.service.club.CauLacBoService;
import com.example.badmintoneventtechnology.service.player.VanDongVienService;
import com.toedter.calendar.JDateChooser;

/**
 * Dialog thêm/sửa Vận động viên
 */
public class VanDongVienDialog extends JDialog {
    private final VanDongVienService vdvService;
    private final CauLacBoService clbService;
    private final VanDongVien original; // null nếu thêm mới
    private final boolean editMode;

    private final JTextField hoTenField = new JTextField(28);
    private final JDateChooser ngaySinhChooser = new JDateChooser();
    private final JComboBox<String> gioiTinhCombo = new JComboBox<>(new String[] { "M - Nam", "F - Nữ", "O - Khác" });
    private final JComboBox<Object> clbCombo = new JComboBox<>(); // chứa item là CLB hoặc "— Không —"

    public VanDongVienDialog(Window parent, String title, VanDongVien vdv,
            VanDongVienService vdvService,
            CauLacBoService clbService) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.vdvService = vdvService;
        this.clbService = clbService;
        this.original = vdv;
        this.editMode = vdv != null;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Fill CLB combo
        clbCombo.addItem("— Không —");
        try {
            List<CauLacBo> clubs = clbService.findAll();
            for (CauLacBo c : clubs) {
                clbCombo.addItem(c);
            }
        } catch (Exception ex) {
            // Không chặn UI; chỉ cảnh báo
            System.err.println("Không thể tải danh sách CLB: " + ex.getMessage());
        }

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        form.add(new JLabel("Họ tên:"));
        form.add(hoTenField);
        form.add(new JLabel("Ngày sinh:"));
        form.add(ngaySinhChooser);
        form.add(new JLabel("Giới tính:"));
        form.add(gioiTinhCombo);
        form.add(new JLabel("Câu lạc bộ:"));
        form.add(clbCombo);
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
            hoTenField.setText(original.getHoTen());
            if (original.getNgaySinh() != null) {
                Date utilDate = Date.from(original.getNgaySinh().atStartOfDay(ZoneId.systemDefault()).toInstant());
                ngaySinhChooser.setDate(utilDate);
            }
            // set gender selection
            String gt = original.getGioiTinh();
            if (gt != null) {
                String up = gt.toUpperCase();
                if (up.equals("M")) gioiTinhCombo.setSelectedIndex(0);
                else if (up.equals("F")) gioiTinhCombo.setSelectedIndex(1);
                else gioiTinhCombo.setSelectedIndex(2);
            }
            // set club selection
            Integer clbId = original.getIdClb();
            if (clbId != null) {
                for (int i = 0; i < clbCombo.getItemCount(); i++) {
                    Object it = clbCombo.getItemAt(i);
                    if (it instanceof CauLacBo c && c.getId() != null && c.getId().equals(clbId)) {
                        clbCombo.setSelectedIndex(i);
                        break;
                    }
                }
            } else {
                clbCombo.setSelectedIndex(0);
            }
        }

        pack();
        setLocationRelativeTo(parent);
    }

    private void onSave() {
        String hoTen = hoTenField.getText() != null ? hoTenField.getText().trim() : "";
        Date utilDate = ngaySinhChooser.getDate();
        LocalDate ngaySinh = (utilDate != null)
                ? utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : null;
        String gioiTinh;
        int idxGt = gioiTinhCombo.getSelectedIndex();
        gioiTinh = switch (idxGt) {
            case 0 -> "M";
            case 1 -> "F";
            default -> "";
        };
        Integer idClb = null;
        Object clbSel = clbCombo.getSelectedItem();
        if (clbSel instanceof CauLacBo c) {
            idClb = c.getId();
        }

        try {
            if (editMode) {
                vdvService.update(original.getId(), hoTen, ngaySinh, idClb, gioiTinh);
            } else {
                vdvService.create(hoTen, ngaySinh, idClb, gioiTinh);
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
