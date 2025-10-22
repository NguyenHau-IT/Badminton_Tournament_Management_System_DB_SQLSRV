package com.example.btms.ui.team;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import com.example.btms.desktop.controller.club.CauLacBoController;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.club.CauLacBo;
import com.example.btms.model.player.VanDongVien;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.service.team.ChiTietDoiService;
import com.example.btms.service.team.DangKiDoiService;
import com.example.btms.ui.player.VanDongVienDialog;

/**
 * Dialog tạo/sửa đội cho một nội dung (đôi) của giải.
 */
public class DangKyDoiDialog extends JDialog {

    private final DangKiDoiService teamService;
    private final ChiTietDoiService detailService;
    private final VanDongVienService vdvService;
    private final CauLacBoService clbService; // vẫn dùng ở onSave()
    private final int idGiai;
    private final List<NoiDung> noiDungOptions; // danh sách ND (chỉ ĐÔI)
    private final NoiDung initialNoiDung; // ND ban đầu (prefill)

    private final Integer editingTeamId; // null nếu tạo mới

    private final JTextField txtTenTeam = new JTextField(24);
    private final JComboBox<Object> cboNoiDung = new JComboBox<>();
    private final JComboBox<Object> cboClb = new JComboBox<>();
    // Dual-list for VĐV selection
    private final DefaultListModel<VanDongVien> modelSelected = new DefaultListModel<>();
    private final DefaultListModel<VanDongVien> modelAvailable = new DefaultListModel<>();
    private final JList<VanDongVien> lstSelected = new JList<>(modelSelected);
    private final JList<VanDongVien> lstAvailable = new JList<>(modelAvailable);
    private List<VanDongVien> lastEligible = new ArrayList<>();

    public DangKyDoiDialog(Window parent,
            Connection conn,
            String title,
            DangKiDoiService teamService,
            ChiTietDoiService detailService,
            VanDongVienService vdvService,
            CauLacBoService clbService,
            int idGiai,
            List<NoiDung> noiDungOptions,
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
        this.noiDungOptions = Objects.requireNonNull(noiDungOptions);
        this.initialNoiDung = Objects.requireNonNull(noiDung);
        this.editingTeamId = editingTeamId;

        setLayout(new BorderLayout(8, 8));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Top form (5 hàng)
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(6, 8, 6, 8);
        form.add(new JLabel("Nội dung:"), gc);
        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        form.add(cboNoiDung, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Tên đội:"), gc);
        gc.gridx = 1;
        gc.gridy = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtTenTeam, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Câu lạc bộ:"), gc);
        gc.gridx = 1;
        gc.gridy = 2;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        form.add(cboClb, gc);
        // Nút thêm CLB bên cạnh combobox CLB
        JButton btnAddClb = new JButton("Thêm CLB");
        GridBagConstraints gcAddClb = new GridBagConstraints();
        gcAddClb.gridx = 2;
        gcAddClb.gridy = 2;
        gcAddClb.insets = new Insets(6, 4, 6, 8);
        gcAddClb.anchor = GridBagConstraints.WEST;
        form.add(btnAddClb, gcAddClb);

        // Hàng chọn VĐV (dual-list)
        gc.gridx = 0;
        gc.gridy = 3;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("VĐV:"), gc);
        gc.gridx = 1;
        gc.gridy = 3;
        gc.weightx = 1;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.BOTH;

        JPanel pickers = new JPanel(new GridBagLayout());
        GridBagConstraints gp = new GridBagConstraints();
        gp.insets = new Insets(2, 2, 2, 2);

        // labels
        gp.gridx = 0;
        gp.gridy = 0;
        gp.anchor = GridBagConstraints.WEST;
        pickers.add(new JLabel("Đã chọn"), gp);
        gp.gridx = 2;
        gp.gridy = 0;
        gp.anchor = GridBagConstraints.WEST;
        pickers.add(new JLabel("Danh sách"), gp);

        // lists with scroll
        lstSelected.setVisibleRowCount(6);
        lstAvailable.setVisibleRowCount(6);
        lstSelected.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstAvailable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstSelected.setPreferredSize(new Dimension(200, 140));
        lstAvailable.setPreferredSize(new Dimension(200, 140));
        JScrollPane spSelected = new JScrollPane(lstSelected);
        JScrollPane spAvailable = new JScrollPane(lstAvailable);

        gp.gridx = 0;
        gp.gridy = 1;
        gp.weightx = 0.5;
        gp.weighty = 1;
        gp.fill = GridBagConstraints.BOTH;
        pickers.add(spSelected, gp);

        JPanel movePanel = new JPanel(new GridBagLayout());
        JButton btnMoveRight = new JButton("<<"); // add to selected (right -> left)
        JButton btnMoveLeft = new JButton(">>"); // remove from selected (left -> right)
        GridBagConstraints gm = new GridBagConstraints();
        gm.insets = new Insets(4, 4, 4, 4);
        gm.gridx = 0;
        gm.gridy = 0;
        movePanel.add(btnMoveRight, gm);
        gm.gridy = 1;
        movePanel.add(btnMoveLeft, gm);

        gp.gridx = 1;
        gp.gridy = 1;
        gp.weightx = 0;
        gp.fill = GridBagConstraints.NONE;
        gp.anchor = GridBagConstraints.CENTER;
        pickers.add(movePanel, gp);

        gp.gridx = 2;
        gp.gridy = 1;
        gp.weightx = 0.5;
        gp.fill = GridBagConstraints.BOTH;
        pickers.add(spAvailable, gp);

        // Button thêm VĐV bên dưới danh sách bên phải
        JButton btnAddVdv = new JButton("Thêm VĐV");
        gp.gridx = 2;
        gp.gridy = 2;
        gp.weightx = 0;
        gp.weighty = 0;
        gp.fill = GridBagConstraints.NONE;
        gp.anchor = GridBagConstraints.EAST;
        pickers.add(btnAddVdv, gp);

        form.add(pickers, gc);
        add(form, BorderLayout.CENTER);
        // Nội dung options (lọc chỉ nội dung ĐÔI đã đăng ký cho giải bằng
        // loadCategories)
        java.util.Set<Integer> allowedDoubleIds = new java.util.LinkedHashSet<>();
        if (conn != null) {
            try {
                java.util.Map<String, Integer>[] maps = new NoiDungRepository(conn).loadCategories(); // [1]=doubles
                allowedDoubleIds.addAll(maps[1].values());
            } catch (Exception ex) {
                System.err.println("Không thể load danh sách nội dung đã đăng ký: " + ex.getMessage());
            }
        }
        for (NoiDung nd : this.noiDungOptions) {
            if (nd == null) {
                continue;
            }
            if (!Boolean.TRUE.equals(nd.getTeam())) {
                continue; // chỉ nội dung đôi
            } // Nếu có danh sách allowed (không rỗng) thì phải thuộc danh sách này
            if (!allowedDoubleIds.isEmpty() && (nd.getId() == null || !allowedDoubleIds.contains(nd.getId()))) {
                continue;
            }
            cboNoiDung.addItem(nd);
        }
        // Nếu đang sửa mà ND ban đầu không nằm trong danh sách đã đăng ký (trường hợp
        // lệch DB) -> thêm vào
        boolean found = false;
        for (int i = 0; i < cboNoiDung.getItemCount(); i++) {
            Object it = cboNoiDung.getItemAt(i);
            if (it instanceof NoiDung x && x.getId() != null && x.getId().equals(initialNoiDung.getId())) {
                found = true;
                break;
            }
        }
        if (!found) {
            cboNoiDung.addItem(initialNoiDung);
        }
        // Renderer để hiển thị tên nội dung
        cboNoiDung.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NoiDung nd) {
                    setText(nd.getTenNoiDung());
                }
                return c;
            }
        });
        // Chọn ND ban đầu
        selectNoiDung(initialNoiDung);
        // Edit mode: không cho đổi ND
        if (editingTeamId != null) {
            cboNoiDung.setEnabled(false);
        }

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
                if (c != null) {
                    cboClb.addItem(c);
                }
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
                if (value instanceof CauLacBo c) {
                    setText(c.getTenClb());
                }
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
                if (value instanceof VanDongVien v) {
                    setText(v.getHoTen());
                }
                return comp;
            }
        };
        lstSelected.setCellRenderer(vRenderer);
        lstAvailable.setCellRenderer(vRenderer);

        // Prefill if editing
        if (tenTeamInit != null) {
            txtTenTeam.setText(tenTeamInit);
        }
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
        // Nạp danh sách VĐV sau khi UI đã khởi tạo xong
        SwingUtilities.invokeLater(() -> updateVdvLists(idVdv1Init, idVdv2Init));
        // Thay đổi CLB => cập nhật lại danh sách VĐV
        cboClb.addActionListener(e -> updateVdvLists(null, null));
        // Thay đổi nội dung => cập nhật lại danh sách VĐV (lọc theo tuổi/giới tính)
        cboNoiDung.addActionListener(e -> updateVdvLists(null, null));
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        // Hành động: Thêm CLB → mở dialog, reload danh sách và chọn CLB mới nếu có
        btnAddClb.addActionListener(e -> {
            Integer beforeMax = getMaxClubIdSafe();
            var ctrl = new CauLacBoController(
                    SwingUtilities.getWindowAncestor(this),
                    "Thêm CLB",
                    null, // null = thêm mới
                    clbService);
            ctrl.open();

            if (ctrl.isSaved()) {
                // List will be reloaded below via reloadClubs(preselect); no local reload() or
                // updateCountLabel() methods exist here.
            }

            // Reload clubs và cố gắng chọn CLB mới
            Integer afterMax = getMaxClubIdSafe();
            Integer preselect = (afterMax != null && beforeMax != null && afterMax > beforeMax) ? afterMax : null;
            reloadClubs(preselect);
            // cập nhật lại danh sách VĐV theo CLB hiện chọn
            updateVdvLists(null, null);
        });

        // Nút di chuyển giữa 2 list
        Runnable addSelected = () -> {
            List<VanDongVien> chosen = lstAvailable.getSelectedValuesList();
            if (chosen == null || chosen.isEmpty()) {
                return;
            }
            // tối đa 2 VĐV
            if (modelSelected.getSize() + chosen.size() > 2) {
                JOptionPane.showMessageDialog(this, "Đội đôi chỉ chọn tối đa 2 VĐV.", "Giới hạn",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            for (VanDongVien v : chosen) {
                if (!containsVdv(modelSelected, v.getId())) {
                    modelSelected.addElement(v);
                }
            }
            rebuildAvailable();
        };
        Runnable removeSelected = () -> {
            List<VanDongVien> rem = lstSelected.getSelectedValuesList();
            if (rem == null || rem.isEmpty()) {
                return;
            }
            for (VanDongVien v : new ArrayList<>(rem)) {
                removeVdv(modelSelected, v.getId());
            }
            rebuildAvailable();
        };
        btnMoveRight.addActionListener(e -> addSelected.run());
        btnMoveLeft.addActionListener(e -> removeSelected.run());
        lstAvailable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addSelected.run();
                }
            }
        });
        lstSelected.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    removeSelected.run();
                }
            }
        });

        // Hành động: Thêm VĐV mới và ưu tiên đưa vào danh sách đã chọn nếu còn chỗ
        btnAddVdv.addActionListener(e -> {
            Integer beforeMax = getMaxPlayerIdSafe();
            VanDongVienDialog v = new VanDongVienDialog(this, "Thêm VĐV", null, vdvService, clbService);
            v.preselectClub(getSelectedClbId());
            NoiDung nd = getSelectedNoiDung();
            String gt = nd != null ? nd.getGioiTinh() : null;
            if (gt != null) {
                v.preselectGender(gt);
            }
            v.setVisible(true);
            Integer afterMax = getMaxPlayerIdSafe();
            Integer preselectId = (afterMax != null && beforeMax != null && afterMax > beforeMax) ? afterMax : null;
            // nếu còn chỗ, ưu tiên thêm vào selected
            if (preselectId != null && modelSelected.getSize() < 2) {
                updateVdvLists(preselectId, null);
            } else {
                updateVdvLists(null, null);
            }
        });

        pack();
        setLocationRelativeTo(parent);
    }

    private void reloadClubs(Integer preselectId) {
        Object current = cboClb.getSelectedItem();
        cboClb.removeAllItems();
        cboClb.addItem("— Không —");
        try {
            for (CauLacBo c : clbService.findAll()) {
                if (c != null) {
                    cboClb.addItem(c);
                }
            }
        } catch (Exception ex) {
            System.err.println("Không thể tải CLB: " + ex.getMessage());
        }
        if (preselectId != null) {
            for (int i = 0; i < cboClb.getItemCount(); i++) {
                Object it = cboClb.getItemAt(i);
                if (it instanceof CauLacBo c && c.getId() != null && c.getId().equals(preselectId)) {
                    cboClb.setSelectedIndex(i);
                    return;
                }
            }
        } else if (current instanceof CauLacBo cCurrent) {
            // giữ lại lựa chọn cũ nếu có thể
            Integer cid = cCurrent.getId();
            for (int i = 0; i < cboClb.getItemCount(); i++) {
                Object it = cboClb.getItemAt(i);
                if (it instanceof CauLacBo c && c.getId() != null && c.getId().equals(cid)) {
                    cboClb.setSelectedIndex(i);
                    return;
                }
            }
        } else {
            cboClb.setSelectedIndex(0);
        }
    }

    private Integer getMaxClubIdSafe() {
        try {
            Integer max = null;
            for (CauLacBo c : clbService.findAll()) {
                if (c == null || c.getId() == null) {
                    continue;
                }
                max = (max == null || c.getId() > max) ? c.getId() : max;
            }
            return max;
        } catch (Exception ex) {
            return null;
        }
    }

    private Integer getMaxPlayerIdSafe() {
        try {
            Integer max = null;
            for (VanDongVien v : vdvService.findAll()) {
                if (v == null || v.getId() == null) {
                    continue;
                }
                max = (max == null || v.getId() > max) ? v.getId() : max;
            }
            return max;
        } catch (Exception ex) {
            return null;
        }
    }

    private void selectNoiDung(NoiDung nd) {
        if (nd == null) {
            return;
        }
        for (int i = 0; i < cboNoiDung.getItemCount(); i++) {
            Object it = cboNoiDung.getItemAt(i);
            if (it instanceof NoiDung x && x.getId() != null && x.getId().equals(nd.getId())) {
                cboNoiDung.setSelectedIndex(i);
                break;
            }
        }
    }

    private NoiDung getSelectedNoiDung() {
        Object sel = cboNoiDung.getSelectedItem();
        return (sel instanceof NoiDung nd) ? nd : initialNoiDung;
    }

    private Integer getSelectedClbId() {
        Object sel = cboClb.getSelectedItem();
        if (sel instanceof CauLacBo c) {
            return c.getId();
        }
        return null; // "— Không —" hoặc không chọn -> không lọc theo CLB
    }

    private void updateVdvLists(Integer keepVdv1Id, Integer keepVdv2Id) {
        Integer clbId = getSelectedClbId();
        NoiDung ndSel = getSelectedNoiDung();
        List<VanDongVien> eligible = new ArrayList<>();

        // Nếu chưa chọn CLB ("— Không —") thì không load danh sách VĐV
        if (clbId == null) {
            modelSelected.clear();
            modelAvailable.clear();
            lastEligible = new ArrayList<>();
            return;
        }
        try {
            List<VanDongVien> all = vdvService.findAll();
            for (VanDongVien v : all) {
                if (v == null) {
                    continue;
                }
                // Lọc theo CLB đã chọn (bắt buộc)
                Integer vidClb = v.getIdClb();
                if (vidClb == null || !clbId.equals(vidClb)) {
                    continue;
                }
                if (!isEligible(v, ndSel)) {
                    continue;
                }
                eligible.add(v);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải VĐV: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
        lastEligible = eligible;
        // Giữ lại các VĐV đã chọn trước đó nhưng chỉ nếu vẫn còn hợp lệ
        List<Integer> keepIds = new ArrayList<>();
        if (keepVdv1Id != null) {
            keepIds.add(keepVdv1Id);
        }
        if (keepVdv2Id != null) {
            keepIds.add(keepVdv2Id);
        }

        // Lấy danh sách đang chọn (chỉ giữ những người còn đủ điều kiện)
        List<VanDongVien> currentSelected = new ArrayList<>();
        for (int i = 0; i < modelSelected.size(); i++) {
            VanDongVien v = modelSelected.get(i);
            if (v != null && containsVdv(eligible, v.getId())) {
                currentSelected.add(v);
            }
        }
        // Bổ sung keepIds nếu có và còn chỗ
        for (Integer kid : keepIds) {
            if (kid == null) {
                continue;
            }
            if (currentSelected.size() >= 2) {
                break;
            }
            VanDongVien kv = findById(eligible, kid);
            if (kv != null && !containsVdv(currentSelected, kv.getId())) {
                currentSelected.add(kv);
            }
        }
        // Cập nhật modelSelected
        modelSelected.clear();
        for (VanDongVien v : currentSelected) {
            modelSelected.addElement(v);
        }
        // Rebuild available = eligible - selected
        rebuildAvailable();
    }

    private void rebuildAvailable() {
        modelAvailable.clear();
        for (VanDongVien v : lastEligible) {
            if (!containsVdv(modelSelected, v.getId())) {
                modelAvailable.addElement(v);
            }
        }
    }

    private static boolean containsVdv(List<VanDongVien> list, Integer id) {
        if (list == null || id == null) {
            return false;
        }
        for (VanDongVien v : list) {
            if (v != null && id.equals(v.getId())) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsVdv(DefaultListModel<VanDongVien> model, Integer id) {
        if (model == null || id == null) {
            return false;
        }
        for (int i = 0; i < model.size(); i++) {
            VanDongVien v = model.get(i);
            if (v != null && id.equals(v.getId())) {
                return true;
            }
        }
        return false;
    }

    private static void removeVdv(DefaultListModel<VanDongVien> model, Integer id) {
        if (model == null || id == null) {
            return;
        }
        for (int i = model.size() - 1; i >= 0; i--) {
            VanDongVien v = model.get(i);
            if (v != null && id.equals(v.getId())) {
                model.remove(i);
            }
        }
    }

    private static VanDongVien findById(List<VanDongVien> list, Integer id) {
        if (list == null || id == null) {
            return null;
        }
        for (VanDongVien v : list) {
            if (v != null && id.equals(v.getId())) {
                return v;
            }
        }
        return null;
    }

    private boolean isEligible(VanDongVien v, NoiDung nd) {
        // Gender filter
        String req = nd.getGioiTinh();
        if (req != null && !req.isBlank()) {
            String gv = v.getGioiTinh();
            if ("M".equalsIgnoreCase(req) && (gv == null || !gv.equalsIgnoreCase("M"))) {
                return false;
            }
            if ("F".equalsIgnoreCase(req) && (gv == null || !gv.equalsIgnoreCase("F"))) {
                return false;
            }
        }
        // Age filter (inclusive); if VDV missing DOB -> allow
        Integer td = nd.getTuoiDuoi();
        Integer tt = nd.getTuoiTren();
        if ((td != null || tt != null) && v.getNgaySinh() != null) {
            int age = Period.between(v.getNgaySinh(), LocalDate.now()).getYears();
            if (td != null && age < td) {
                return false;
            }
            if (tt != null && tt > 0 && age > tt) {
                return false;
            }
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
        if (clbSel instanceof CauLacBo c) {
            idClb = c.getId();
        }

        // Yêu cầu phải chọn đúng 2 VĐV
        if (modelSelected.getSize() != 2) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đủ 2 VĐV.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer id1 = modelSelected.get(0).getId();
        Integer id2 = modelSelected.get(1).getId();

        try {
            NoiDung ndSel = getSelectedNoiDung();

            // Kiểm tra trùng tên đội trong cùng (giải, nội dung, CLB)
            try {
                List<com.example.btms.model.team.DangKiDoi> exist = teamService.listTeams(idGiai, ndSel.getId());
                final Integer selClbId = idClb; // có thể null
                boolean dup = exist.stream().anyMatch(t -> {
                    if (t == null || t.getTenTeam() == null) {
                        return false;
                    }
                    boolean sameName = t.getTenTeam().trim().equalsIgnoreCase(ten);
                    boolean sameClub = java.util.Objects.equals(t.getIdCauLacBo(), selClbId);
                    boolean notSelf = (editingTeamId == null)
                            || !java.util.Objects.equals(t.getIdTeam(), editingTeamId);
                    // Chỉ chặn nếu trùng TÊN và cùng CLB trong cùng nội dung của giải
                    return sameName && sameClub && notSelf;
                });
                if (dup) {
                    JOptionPane.showMessageDialog(this,
                            "Tên đội đã tồn tại trong nội dung này của cùng CLB.\n" +
                                    "Vui lòng đổi tên khác.",
                            "Trùng tên đội",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (RuntimeException ignore) {
                // Bỏ qua nếu không kiểm tra được, sẽ để DB kiểm tra (nếu có ràng buộc)
            }

            if (editingTeamId == null) {
                int newTeamId = teamService.createTeam(idGiai, ndSel.getId(), idClb, ten);
                detailService.replaceMembers(newTeamId, List.of(id1, id2));
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
            // Hiển thị nguyên nhân gốc để dễ chẩn đoán
            Throwable root = ex;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            String msg = ex.getMessage();
            String rootMsg = root != ex ? root.getMessage() : null;
            String full = (msg != null ? msg : "Lỗi") + (rootMsg != null ? "\nChi tiết: " + rootMsg : "");
            JOptionPane.showMessageDialog(this, full, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
