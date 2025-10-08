package com.example.btms.ui.player;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.example.btms.model.club.CauLacBo;
import com.example.btms.model.player.VanDongVien;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.player.VanDongVienService;
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
    private final JComboBox<String> gioiTinhCombo = new JComboBox<>(new String[] { "m - Nam", "f - Nữ" });
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
        setLayout(new BorderLayout(0, 0));

        // Fill CLB combo
        clbCombo.addItem("— Không —");
        try {
            List<CauLacBo> clubs = this.clbService.findAll();
            for (CauLacBo c : clubs) {
                if (c != null) {
                    clbCombo.addItem(c);
                }
            }
        } catch (RuntimeException ex) {
            // Không chặn UI; chỉ cảnh báo
            System.err.println("Không thể tải danh sách CLB: " + ex.getMessage());
        }
        // Hiển thị tên CLB trong dropdown
        clbCombo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                java.awt.Component comp = super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                if (value instanceof CauLacBo c) {
                    setText(c.getTenClb() != null ? c.getTenClb() : "(Không tên)");
                }
                return comp;
            }
        });

        // Form panel (gọn gàng, canh hàng) với GridBagLayout
        JPanel formWrap = new JPanel(new BorderLayout());
        formWrap.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Thông tin VĐV"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Họ tên
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Họ tên:"), gc);
        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        hoTenField.setToolTipText("Nhập họ tên đầy đủ của VĐV");
        form.add(hoTenField, gc);

        // Ngày sinh
        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Ngày sinh:"), gc);
        gc.gridx = 1;
        gc.gridy = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        ngaySinhChooser.setDateFormatString("dd/MM/yyyy");
        ngaySinhChooser.setToolTipText("Định dạng: dd/MM/yyyy (có thể để trống)");
        form.add(ngaySinhChooser, gc);

        // Giới tính
        gc.gridx = 0;
        gc.gridy = 2;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Giới tính:"), gc);
        gc.gridx = 1;
        gc.gridy = 2;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gioiTinhCombo.setToolTipText("Chọn giới tính của VĐV");
        form.add(gioiTinhCombo, gc);

        // Câu lạc bộ
        gc.gridx = 0;
        gc.gridy = 3;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Câu lạc bộ:"), gc);
        gc.gridx = 1;
        gc.gridy = 3;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        clbCombo.setToolTipText("Chọn câu lạc bộ (có thể để ‘Không’)");
        form.add(clbCombo, gc);

        formWrap.add(form, BorderLayout.CENTER);
        add(formWrap, BorderLayout.CENTER);

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
            hoTenField.setText(original.getHoTen());
            if (original.getNgaySinh() != null) {
                Date utilDate = Date.from(original.getNgaySinh().atStartOfDay(ZoneId.systemDefault()).toInstant());
                ngaySinhChooser.setDate(utilDate);
            }
            // set gender selection
            String gt = original.getGioiTinh();
            if (gt != null) {
                if (gt.equalsIgnoreCase("m"))
                    gioiTinhCombo.setSelectedIndex(0);
                else if (gt.equalsIgnoreCase("f"))
                    gioiTinhCombo.setSelectedIndex(1);
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

        // Phím tắt và default button
        getRootPane().setDefaultButton(btnSave);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(parent);
    }

    private void onSave() {
        String hoTen = hoTenField.getText() != null ? hoTenField.getText().trim() : "";
        if (hoTen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập họ tên.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            hoTenField.requestFocus();
            return;
        }
        Date utilDate = ngaySinhChooser.getDate();
        LocalDate ngaySinh = (utilDate != null)
                ? utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : null;
        String gioiTinh;
        int idxGt = gioiTinhCombo.getSelectedIndex();
        gioiTinh = switch (idxGt) {
            case 0 -> "m";
            case 1 -> "f";
            default -> null;
        };
        Integer idClb = null;
        Object clbSel = clbCombo.getSelectedItem();
        if (clbSel instanceof CauLacBo c) {
            idClb = c.getId();
        }

        try {
            if (editMode) {
                vdvService.update(original.getId(), hoTen, ngaySinh, idClb, gioiTinh);
                JOptionPane.showMessageDialog(this, "Cập nhật VĐV thành công.", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                vdvService.create(hoTen, ngaySinh, idClb, gioiTinh);
                JOptionPane.showMessageDialog(this, "Thêm VĐV thành công.", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            dispose();
        } catch (IllegalArgumentException | IllegalStateException | java.util.NoSuchElementException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Prefill CLB theo id (chỉ áp dụng khi thêm mới). Nếu id=null thì chọn "— Không
     * —".
     */
    public void preselectClub(Integer clbId) {
        if (editMode)
            return; // không thay đổi khi đang sửa
        if (clbId == null) {
            clbCombo.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < clbCombo.getItemCount(); i++) {
            Object it = clbCombo.getItemAt(i);
            if (it instanceof CauLacBo c && c.getId() != null && c.getId().equals(clbId)) {
                clbCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Prefill giới tính theo mã ("m" hoặc "f") khi thêm mới.
     */
    public void preselectGender(String code) {
        if (editMode || code == null)
            return;
        if (code.equalsIgnoreCase("m")) {
            gioiTinhCombo.setSelectedIndex(0);
        } else if (code.equalsIgnoreCase("f")) {
            gioiTinhCombo.setSelectedIndex(1);
        }
    }
}
