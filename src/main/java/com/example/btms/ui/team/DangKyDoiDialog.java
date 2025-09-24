package com.example.btms.ui.team;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import com.example.btms.model.category.NoiDung;
import com.example.btms.model.club.CauLacBo;
import com.example.btms.model.player.VanDongVien;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.service.team.ChiTietDoiService;
import com.example.btms.service.team.DangKiDoiService;

/** Dialog tạo/sửa đội cho một nội dung (đôi) của giải. */
public class DangKyDoiDialog extends JDialog {
    private final DangKiDoiService teamService;
    private final ChiTietDoiService detailService;
    private final VanDongVienService vdvService;
    private final CauLacBoService clbService;
    private final int idGiai;
    private final NoiDung noiDung; // đã đảm bảo là nội dung đôi

    private final Integer editingTeamId; // null nếu tạo mới

    private final JTextField txtTenTeam = new JTextField(24);
    private final JComboBox<Object> cboClb = new JComboBox<>();
    private final JComboBox<Object> cboVdv1 = new JComboBox<>();
    private final JComboBox<Object> cboVdv2 = new JComboBox<>();

    public DangKyDoiDialog(Window parent,
            String title,
            DangKiDoiService teamService,
            ChiTietDoiService detailService,
            VanDongVienService vdvService,
            CauLacBoService clbService,
            int idGiai,
            NoiDung noiDung,
            Integer editingTeamId,
            String tenTeamInit,
            Integer idClbInit,
            Integer idVdv1Init,
            Integer idVdv2Init) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.teamService = Objects.requireNonNull(teamService);
        this.detailService = Objects.requireNonNull(detailService);
        this.vdvService = Objects.requireNonNull(vdvService);
        this.clbService = Objects.requireNonNull(clbService);
        this.idGiai = idGiai;
        this.noiDung = Objects.requireNonNull(noiDung);
        this.editingTeamId = editingTeamId;

        setLayout(new BorderLayout(8, 8));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Top form
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        form.add(new JLabel("Nội dung:"));
        form.add(new JLabel(noiDung.getTenNoiDung()));
        form.add(new JLabel("Tên đội:"));
        form.add(txtTenTeam);
        form.add(new JLabel("Câu lạc bộ:"));
        form.add(cboClb);
        form.add(new JLabel("VĐV 1:"));
        form.add(cboVdv1);
        form.add(new JLabel("VĐV 2:"));
        form.add(cboVdv2);
        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        buttons.add(btnSave);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        // Init data
        cboClb.addItem("— Không —");
        try {
            for (CauLacBo c : clbService.findAll()) {
                if (c != null)
                    cboClb.addItem(c);
            }
        } catch (Exception ex) {
            System.err.println("Không thể tải CLB: " + ex.getMessage());
        }
        // renderer
        cboClb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component comp = super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                if (value instanceof CauLacBo c)
                    setText(c.getTenClb());
                return comp;
            }
        });

        // Renderer cho VĐV
        ListCellRenderer<Object> vRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component comp = super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                if (value instanceof VanDongVien v)
                    setText(v.getHoTen());
                return comp;
            }
        };
        cboVdv1.setRenderer(vRenderer);
        cboVdv2.setRenderer(vRenderer);

        // Prefill if editing
        if (tenTeamInit != null)
            txtTenTeam.setText(tenTeamInit);
        if (idClbInit != null) {
            for (int i = 0; i < cboClb.getItemCount(); i++) {
                Object it = cboClb.getItemAt(i);
                if (it instanceof CauLacBo c && c.getId() != null && c.getId().equals(idClbInit)) {
                    cboClb.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            cboClb.setSelectedIndex(0);
        }
        // Nạp danh sách VĐV theo CLB đang chọn + điều kiện nội dung, rồi chọn sẵn nếu
        // có
        updateVdvCombos(idVdv1Init, idVdv2Init);
        // Thay đổi CLB => cập nhật lại danh sách VĐV
        cboClb.addActionListener(e -> updateVdvCombos(null, null));
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(parent);
    }

    private Integer getSelectedClbId() {
        Object sel = cboClb.getSelectedItem();
        if (sel instanceof CauLacBo c)
            return c.getId();
        return null; // "— Không —" hoặc không chọn -> không lọc theo CLB
    }

    private void updateVdvCombos(Integer keepVdv1Id, Integer keepVdv2Id) {
        Integer clbId = getSelectedClbId();
        List<VanDongVien> eligible = new ArrayList<>();
        try {
            List<VanDongVien> all = vdvService.findAll();
            for (VanDongVien v : all) {
                if (v == null)
                    continue;
                // lọc theo CLB nếu đã chọn CLB (không lọc nếu CLB null)
                if (clbId != null) {
                    Integer vidClb = v.getIdClb();
                    if (vidClb == null || !clbId.equals(vidClb))
                        continue;
                }
                if (!isEligible(v, noiDung))
                    continue;
                eligible.add(v);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải VĐV: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Refill combos
        cboVdv1.removeAllItems();
        cboVdv2.removeAllItems();
        cboVdv1.addItem("— Chọn —");
        cboVdv2.addItem("— Chọn —");
        for (VanDongVien v : eligible) {
            cboVdv1.addItem(v);
            cboVdv2.addItem(v);
        }
        if (keepVdv1Id != null)
            selectVdv(cboVdv1, keepVdv1Id);
        if (keepVdv2Id != null)
            selectVdv(cboVdv2, keepVdv2Id);
    }

    private void selectVdv(JComboBox<Object> combo, int idVdv) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object it = combo.getItemAt(i);
            if (it instanceof VanDongVien v && v.getId() != null && v.getId() == idVdv) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private boolean isEligible(VanDongVien v, NoiDung nd) {
        // Gender filter
        String req = nd.getGioiTinh();
        if (req != null && !req.isBlank()) {
            String gv = v.getGioiTinh();
            if ("M".equalsIgnoreCase(req) && (gv == null || !gv.equalsIgnoreCase("M")))
                return false;
            if ("F".equalsIgnoreCase(req) && (gv == null || !gv.equalsIgnoreCase("F")))
                return false;
        }
        // Age filter (inclusive); if VDV missing DOB -> allow
        Integer td = nd.getTuoiDuoi();
        Integer tt = nd.getTuoiTren();
        if ((td != null || tt != null) && v.getNgaySinh() != null) {
            int age = Period.between(v.getNgaySinh(), LocalDate.now()).getYears();
            if (td != null && age < td)
                return false;
            if (tt != null && tt > 0 && age > tt)
                return false;
        }
        return true;
    }

    private void onSave() {
        String ten = txtTenTeam.getText() != null ? txtTenTeam.getText().trim() : "";
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên đội.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            txtTenTeam.requestFocus();
            return;
        }
        Integer idClb = null;
        Object clbSel = cboClb.getSelectedItem();
        if (clbSel instanceof CauLacBo c)
            idClb = c.getId();

        Integer id1 = null, id2 = null;
        Object s1 = cboVdv1.getSelectedItem();
        Object s2 = cboVdv2.getSelectedItem();
        if (s1 instanceof VanDongVien v1)
            id1 = v1.getId();
        if (s2 instanceof VanDongVien v2)
            id2 = v2.getId();
        if (id1 == null || id2 == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đủ 2 VĐV.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (Objects.equals(id1, id2)) {
            JOptionPane.showMessageDialog(this, "Hai VĐV phải khác nhau.", "Sai dữ liệu",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (editingTeamId == null) {
                detailService.replaceMembers(teamService.createTeam(idGiai, noiDung.getId(), idClb, ten),
                        List.of(id1, id2));
                JOptionPane.showMessageDialog(this, "Thêm đội thành công.", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                teamService.updateTeamInfo(editingTeamId, idClb, ten);
                detailService.replaceMembers(editingTeamId, List.of(id1, id2));
                JOptionPane.showMessageDialog(this, "Cập nhật đội thành công.", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
