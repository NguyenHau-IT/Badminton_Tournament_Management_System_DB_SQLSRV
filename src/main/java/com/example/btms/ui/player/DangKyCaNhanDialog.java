package com.example.btms.ui.player;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.example.btms.model.category.NoiDung;
import com.example.btms.model.club.CauLacBo;
import com.example.btms.model.player.DangKiCaNhan;
import com.example.btms.model.player.VanDongVien;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.player.DangKiCaNhanService;
import com.example.btms.service.player.VanDongVienService;

/** Dialog thêm / sửa đăng ký cá nhân */
public class DangKyCaNhanDialog extends JDialog {
    private final DangKiCaNhanService dkService;
    private final VanDongVienService vdvService;
    private final NoiDungService noiDungService;
    private final int idGiai;
    private final DangKiCaNhan editing;
    private final Connection conn; // để lọc nội dung đã đăng ký
    private final CauLacBoService clbService;

    private final JComboBox<NoiDung> cboNoiDung = new JComboBox<>();
    private final JComboBox<Object> cboClb = new JComboBox<>();
    private final JComboBox<VanDongVien> cboVdv = new JComboBox<>();

    public DangKyCaNhanDialog(Window owner, String title, int idGiai, Connection conn,
            DangKiCaNhanService dkService,
            VanDongVienService vdvService, NoiDungService ndService,
            CauLacBoService clbService,
            DangKiCaNhan editing) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.idGiai = idGiai;
        this.conn = conn;
        this.dkService = dkService;
        this.vdvService = vdvService;
        this.noiDungService = ndService;
        this.clbService = clbService;
        this.editing = editing;
        buildUI();
        // Trì hoãn load dữ liệu sau khi constructor hoàn tất để tránh cảnh báo leak
        // 'this'
        java.awt.EventQueue.invokeLater(() -> {
            String loadError = null;
            try {
                loadData();
                if (editing != null)
                    prefill();
            } catch (java.sql.SQLException ex) {
                loadError = "Lỗi tải dữ liệu SQL: " + ex.getMessage();
            } catch (RuntimeException ex) {
                loadError = "Lỗi tải dữ liệu: " + ex.getMessage();
            }
            pack();
            setLocationRelativeTo(owner);
            if (loadError != null) {
                JOptionPane.showMessageDialog(getOwner(), loadError, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void buildUI() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Đăng ký cá nhân"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Nội dung
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Nội dung (đơn):"), gc);
        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        cboNoiDung.setToolTipText("Chọn nội dung thi đấu đơn");
        form.add(cboNoiDung, gc);

        // Câu lạc bộ
        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Câu lạc bộ:"), gc);
        gc.gridx = 1;
        gc.gridy = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        cboClb.setToolTipText("Chọn câu lạc bộ (bắt buộc để hiện danh sách VĐV)");
        form.add(cboClb, gc);

        // Vận động viên
        gc.gridx = 0;
        gc.gridy = 2;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Vận động viên:"), gc);
        gc.gridx = 1;
        gc.gridy = 2;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        cboVdv.setToolTipText("Chọn VĐV (chỉ xuất hiện khi đã chọn CLB)");
        form.add(cboVdv, gc);

        wrap.add(form, BorderLayout.CENTER);

        JButton btnOk = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        btnOk.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.add(btnOk);
        buttons.add(btnCancel);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        bottom.add(buttons, BorderLayout.EAST);

        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().add(wrap, BorderLayout.CENTER);
        getContentPane().add(bottom, BorderLayout.SOUTH);

        // Default button & ESC để đóng nhanh
        getRootPane().setDefaultButton(btnOk);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void loadData() throws java.sql.SQLException {
        DefaultComboBoxModel<NoiDung> ndModel = new DefaultComboBoxModel<>();
        // Chỉ lấy nội dung đã đăng ký cho giải (sử dụng loadCategories của repository)
        if (conn != null) {
            NoiDungRepository repo = new NoiDungRepository(conn);
            java.util.Map<String, Integer>[] maps = repo.loadCategories(); // maps[0]=singles
            Set<Integer> singlesIds = new HashSet<>(maps[0].values());
            for (NoiDung nd : noiDungService.getAllNoiDung()) {
                if (singlesIds.contains(nd.getId()) && !Boolean.TRUE.equals(nd.getTeam())) {
                    ndModel.addElement(nd);
                }
            }
        }
        // fallback nếu vì lý do nào đó chưa có dữ liệu
        if (ndModel.getSize() == 0) {
            for (NoiDung nd : noiDungService.getAllNoiDung()) {
                if (!Boolean.TRUE.equals(nd.getTeam()))
                    ndModel.addElement(nd);
            }
        }
        cboNoiDung.setModel(ndModel);
        loadClubs();
        // Ban đầu để trống danh sách VĐV cho đến khi chọn CLB
        clearPlayers();
        cboClb.addActionListener(e -> reloadPlayersForSelectedClub(null));
        cboNoiDung.addActionListener(e -> reloadPlayersForSelectedClub(null)); // đổi nội dung -> lọc lại theo giới
        // Renderer hiển thị chỉ tên VĐV
        cboVdv.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                    int index,
                    boolean isSelected, boolean cellHasFocus) {
                java.awt.Component c = super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                if (value instanceof VanDongVien v) {
                    setText(v.getHoTen());
                }
                return c;
            }
        });
        // Không nạp VĐV khi chưa chọn CLB; đợi sự kiện chọn CLB
    }

    private void loadClubs() {
        cboClb.removeAllItems();
        cboClb.addItem("— Chọn CLB —");
        if (clbService == null)
            return;
        try {
            for (CauLacBo c : clbService.findAll()) {
                if (c != null)
                    cboClb.addItem(c);
            }
        } catch (Exception ex) {
            System.err.println("Không thể tải CLB: " + ex.getMessage());
        }
        // Renderer tên CLB
        cboClb.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                java.awt.Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CauLacBo club) {
                    setText(club.getTenClb());
                }
                return c;
            }
        });
    }

    private void clearPlayers() {
        cboVdv.setModel(new DefaultComboBoxModel<>());
    }

    private void reloadPlayersForSelectedClub(Integer preselectId) {
        DefaultComboBoxModel<VanDongVien> vdvModel = new DefaultComboBoxModel<>();
        try {
            // Yêu cầu phải chọn CLB thì mới nạp danh sách VĐV
            Object sel = cboClb.getSelectedItem();
            if (!(sel instanceof CauLacBo club)) {
                clearPlayers();
                return;
            }

            NoiDung ndSel = (NoiDung) cboNoiDung.getSelectedItem();
            String reqGender = ndSel != null ? ndSel.getGioiTinh() : null; // "M" hoặc "F" hoặc null
            Integer tuoiDuoi = ndSel != null ? ndSel.getTuoiDuoi() : null;
            Integer tuoiTren = ndSel != null ? ndSel.getTuoiTren() : null; // 0 hoặc null = không giới hạn trên
            Integer clubId = club.getId();

            for (VanDongVien v : vdvService.findAll()) {
                if (v == null)
                    continue;
                // Bắt buộc cùng CLB
                if (v.getIdClb() == null || !v.getIdClb().equals(clubId))
                    continue;
                // Giới tính theo nội dung
                if (reqGender != null && !reqGender.isBlank()) {
                    String gv = v.getGioiTinh();
                    if ("M".equalsIgnoreCase(reqGender) && (gv == null || !gv.equalsIgnoreCase("M")))
                        continue;
                    if ("F".equalsIgnoreCase(reqGender) && (gv == null || !gv.equalsIgnoreCase("F")))
                        continue;
                }
                // Tuổi theo nội dung (nếu có)
                if ((tuoiDuoi != null || (tuoiTren != null && tuoiTren > 0)) && v.getNgaySinh() != null) {
                    try {
                        LocalDate dob = v.getNgaySinh();
                        int age = Period.between(dob, LocalDate.now()).getYears();
                        if (tuoiDuoi != null && age < tuoiDuoi)
                            continue;
                        if (tuoiTren != null && tuoiTren > 0 && age > tuoiTren)
                            continue;
                    } catch (Exception ignore) {
                        // nếu parse lỗi vẫn cho qua
                    }
                }
                vdvModel.addElement(v);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải VĐV: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        cboVdv.setModel(vdvModel);
        if (preselectId != null) {
            for (int i = 0; i < cboVdv.getItemCount(); i++) {
                VanDongVien v = cboVdv.getItemAt(i);
                if (v.getId().equals(preselectId)) {
                    cboVdv.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void prefill() {
        // Chọn lại combobox dựa trên editing
        for (int i = 0; i < cboNoiDung.getItemCount(); i++) {
            if (cboNoiDung.getItemAt(i).getId().equals(editing.getIdNoiDung())) {
                cboNoiDung.setSelectedIndex(i);
                break;
            }
        }
        // Xác định CLB của VĐV đang chỉnh sửa để chọn trước
        try {
            VanDongVien current = vdvService.findOne(editing.getIdVdv());
            if (current != null && current.getIdClb() != null) {
                // chọn CLB
                for (int i = 0; i < cboClb.getItemCount(); i++) {
                    Object it = cboClb.getItemAt(i);
                    if (it instanceof CauLacBo club && club.getId() != null
                            && club.getId().equals(current.getIdClb())) {
                        cboClb.setSelectedIndex(i);
                        break;
                    }
                }
                // load lại VĐV của CLB đó và chọn VĐV
                reloadPlayersForSelectedClub(editing.getIdVdv());
            }
        } catch (Exception ex) {
            System.err.println("Không thể prefill CLB/VĐV: " + ex.getMessage());
        }
    }

    private void onSave() {
        NoiDung nd = (NoiDung) cboNoiDung.getSelectedItem();
        VanDongVien v = (VanDongVien) cboVdv.getSelectedItem();
        if (nd == null || v == null) {
            JOptionPane.showMessageDialog(this, "Chọn đầy đủ nội dung, CLB và VĐV");
            return;
        }
        try {
            if (editing == null) {
                dkService.register(idGiai, nd.getId(), v.getId());
            } else {
                // Khóa mới
                int newGiai = idGiai;
                int newNd = nd.getId();
                int newV = v.getId();
                dkService.register(newGiai, newNd, newV, LocalDateTime.now(), false);
            }
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
