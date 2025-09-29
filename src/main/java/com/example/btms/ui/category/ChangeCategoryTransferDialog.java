package com.example.btms.ui.category;

import com.example.btms.config.Prefs;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.team.ChiTietDoi;
import com.example.btms.model.team.DangKiDoi;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.player.DangKiCaNhanService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.service.team.ChiTietDoiService;
import com.example.btms.service.team.DangKiDoiService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Dialog chuyển đăng ký giữa các nội dung theo giao diện 2 cột (trái -> phải).
 * Hỗ trợ cả chế độ CÁ NHÂN (singles) và ĐỘI (đôi).
 */
public class ChangeCategoryTransferDialog extends JDialog {
    private final Prefs prefs;
    private final boolean teamMode; // true=đội, false=đơn
    private final List<NoiDung> categories; // đã lọc sẵn theo mode

    // Services cho từng chế độ (có thể null nếu không dùng)
    private final DangKiCaNhanService dkCaNhanService;
    private final VanDongVienService vdvService;
    private final DangKiDoiService doiService;
    private final ChiTietDoiService chiTietDoiService;
    private final CauLacBoService clbService;

    private final Runnable onChanged;

    // UI
    private final JComboBox<NoiDung> cboLeft = new JComboBox<>();
    private final JComboBox<NoiDung> cboRight = new JComboBox<>();
    private final DefaultTableModel leftModel = new DefaultTableModel(new Object[] { "ID", "Tên", "CLB", "Thành viên" },
            0) {
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

    public ChangeCategoryTransferDialog(Window owner,
            Prefs prefs,
            boolean teamMode,
            List<NoiDung> categories,
            NoiDung initialLeft,
            // singles services
            DangKiCaNhanService dkCaNhanService,
            VanDongVienService vdvService,
            // teams services
            DangKiDoiService doiService,
            ChiTietDoiService chiTietDoiService,
            CauLacBoService clbService,
            Runnable onChanged) {
        super(owner, teamMode ? "Chuyển nội dung đội" : "Chuyển nội dung đơn", ModalityType.MODELESS);
        this.prefs = Objects.requireNonNull(prefs);
        this.teamMode = teamMode;
        this.categories = Objects.requireNonNull(categories);
        this.dkCaNhanService = dkCaNhanService;
        this.vdvService = vdvService;
        this.doiService = doiService;
        this.chiTietDoiService = chiTietDoiService;
        this.clbService = clbService;
        this.onChanged = onChanged != null ? onChanged : () -> {
        };

        setLayout(new BorderLayout(10, 10));

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

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0.5;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        center.add(new JScrollPane(tblLeft), gc);

        JPanel midButtons = new JPanel(new GridLayout(3, 1, 0, 8));
        JButton btnToRight = new JButton(">>");
        JButton btnRefresh = new JButton("Tải lại");
        JButton btnClose = new JButton("Đóng");
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
        gc.weightx = 0.5;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        center.add(new JScrollPane(tblRight), gc);
        add(center, BorderLayout.CENTER);

        // Bind combo data
        DefaultComboBoxModel<NoiDung> model = new DefaultComboBoxModel<>(this.categories.toArray(NoiDung[]::new));
        cboLeft.setModel(model);
        cboRight.setModel(new DefaultComboBoxModel<>(this.categories.toArray(NoiDung[]::new)));
        if (initialLeft != null) {
            selectCombo(cboLeft, initialLeft.getId());
        } else if (cboLeft.getItemCount() > 0) {
            cboLeft.setSelectedIndex(0);
        }
        if (cboRight.getItemCount() > 0) {
            // mặc định chọn phần tử khác trái nếu có
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

        // Load initial lists
        cboLeft.addActionListener(e -> reloadLeft());
        cboRight.addActionListener(e -> reloadRight());
        btnRefresh.addActionListener(e -> {
            reloadLeft();
            reloadRight();
        });
        btnClose.addActionListener(e -> dispose());
        btnToRight.addActionListener(e -> moveLeftToRight());

        reloadLeft();
        reloadRight();

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

    private void reloadLeft() {
        leftModel.setRowCount(0);
        NoiDung left = (NoiDung) cboLeft.getSelectedItem();
        if (left == null)
            return;
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (idGiai <= 0)
            return;
        try {
            if (teamMode) {
                List<DangKiDoi> teams = Objects.requireNonNull(doiService).listTeams(idGiai, left.getId());
                for (DangKiDoi t : teams) {
                    String clbName = "";
                    try {
                        var teamFull = Objects.requireNonNull(doiService)
                                .getTeam(Objects.requireNonNull(t.getIdTeam()));
                        if (teamFull.getIdCauLacBo() != null) {
                            try {
                                var clb = Objects.requireNonNull(clbService).findOne(teamFull.getIdCauLacBo());
                                clbName = clb != null ? clb.getTenClb() : "";
                            } catch (Exception ignore) {
                            }
                        }
                    } catch (Exception ignore) {
                    }
                    String members;
                    try {
                        List<ChiTietDoi> list = Objects.requireNonNull(chiTietDoiService)
                                .listMembers(Objects.requireNonNull(t.getIdTeam()));
                        if (list.isEmpty()) {
                            members = "(chưa có thành viên)";
                        } else {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < list.size(); i++) {
                                var m = list.get(i);
                                try {
                                    var v = Objects.requireNonNull(vdvService).findOne(m.getIdVdv());
                                    if (i > 0)
                                        sb.append(" & ");
                                    sb.append(v.getHoTen());
                                } catch (Exception ignore) {
                                }
                            }
                            members = sb.toString();
                        }
                    } catch (RuntimeException ex) {
                        members = "(lỗi lấy thành viên)";
                    }
                    leftModel.addRow(new Object[] { t.getIdTeam(), t.getTenTeam(), clbName, members });
                }
            } else {
                Map<String, Integer> map = Objects.requireNonNull(vdvService).loadSinglesNames(left.getId(), idGiai);
                for (Map.Entry<String, Integer> e : map.entrySet()) {
                    String clbName = "";
                    try {
                        var v = Objects.requireNonNull(vdvService).findOne(e.getValue());
                        if (v.getIdClb() != null) {
                            try {
                                var clb = Objects.requireNonNull(clbService).findOne(v.getIdClb());
                                clbName = clb != null ? clb.getTenClb() : "";
                            } catch (Exception ignore) {
                            }
                        }
                    } catch (RuntimeException ignore) {
                    }
                    leftModel.addRow(new Object[] { e.getValue(), e.getKey(), clbName, "" });
                }
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách trái: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reloadRight() {
        rightModel.setRowCount(0);
        NoiDung right = (NoiDung) cboRight.getSelectedItem();
        if (right == null)
            return;
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (idGiai <= 0)
            return;
        try {
            if (teamMode) {
                List<DangKiDoi> teams = Objects.requireNonNull(doiService).listTeams(idGiai, right.getId());
                for (DangKiDoi t : teams) {
                    String clbName = "";
                    try {
                        var teamFull = Objects.requireNonNull(doiService)
                                .getTeam(Objects.requireNonNull(t.getIdTeam()));
                        if (teamFull.getIdCauLacBo() != null) {
                            try {
                                var clb = Objects.requireNonNull(clbService).findOne(teamFull.getIdCauLacBo());
                                clbName = clb != null ? clb.getTenClb() : "";
                            } catch (Exception ignore) {
                            }
                        }
                    } catch (Exception ignore) {
                    }
                    String members;
                    try {
                        List<ChiTietDoi> list = Objects.requireNonNull(chiTietDoiService)
                                .listMembers(Objects.requireNonNull(t.getIdTeam()));
                        if (list.isEmpty()) {
                            members = "(chưa có thành viên)";
                        } else {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < list.size(); i++) {
                                var m = list.get(i);
                                try {
                                    var v = Objects.requireNonNull(vdvService).findOne(m.getIdVdv());
                                    if (i > 0)
                                        sb.append(" & ");
                                    sb.append(v.getHoTen());
                                } catch (Exception ignore) {
                                }
                            }
                            members = sb.toString();
                        }
                    } catch (RuntimeException ex) {
                        members = "(lỗi lấy thành viên)";
                    }
                    rightModel.addRow(new Object[] { t.getIdTeam(), t.getTenTeam(), clbName, members });
                }
            } else {
                Map<String, Integer> map = Objects.requireNonNull(vdvService).loadSinglesNames(right.getId(), idGiai);
                for (Map.Entry<String, Integer> e : map.entrySet()) {
                    String clbName = "";
                    try {
                        var v = Objects.requireNonNull(vdvService).findOne(e.getValue());
                        if (v.getIdClb() != null) {
                            try {
                                var clb = Objects.requireNonNull(clbService).findOne(v.getIdClb());
                                clbName = clb != null ? clb.getTenClb() : "";
                            } catch (Exception ignore) {
                            }
                        }
                    } catch (RuntimeException ignore) {
                    }
                    rightModel.addRow(new Object[] { e.getValue(), e.getKey(), clbName, "" });
                }
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách phải: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void moveLeftToRight() {
        NoiDung left = (NoiDung) cboLeft.getSelectedItem();
        NoiDung right = (NoiDung) cboRight.getSelectedItem();
        if (left == null || right == null) {
            JOptionPane.showMessageDialog(this, "Chọn nội dung trái và phải.");
            return;
        }
        if (Objects.equals(left.getId(), right.getId())) {
            JOptionPane.showMessageDialog(this, "Hai nội dung đang trùng nhau.");
            return;
        }
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (idGiai <= 0) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
            return;
        }

        int[] viewRows = tblLeft.getSelectedRows();
        if (viewRows == null || viewRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Chọn dòng ở bảng bên trái để chuyển.");
            return;
        }

        int moved = 0, skipped = 0;
        for (int vr : viewRows) {
            int mr = tblLeft.convertRowIndexToModel(vr);
            Integer id = (Integer) leftModel.getValueAt(mr, 0); // cột 0 là ID
            try {
                if (teamMode) {
                    Objects.requireNonNull(doiService).updateTeamCategory(id, right.getId());
                    moved++;
                } else {
                    // tránh trùng đăng ký ở nội dung phải
                    if (Objects.requireNonNull(dkCaNhanService).exists(idGiai, right.getId(), id)) {
                        skipped++;
                        continue;
                    }
                    dkCaNhanService.unregister(idGiai, left.getId(), id);
                    dkCaNhanService.register(idGiai, right.getId(), id);
                    moved++;
                }
            } catch (Exception ex) {
                skipped++;
            }
        }
        if (moved > 0) {
            reloadLeft();
            reloadRight();
            onChanged.run();
        }
        String msg = "Đã chuyển: " + moved + (skipped > 0 ? ", bỏ qua: " + skipped : "");
        JOptionPane.showMessageDialog(this, msg);
    }
}
