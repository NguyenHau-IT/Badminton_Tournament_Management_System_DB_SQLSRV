package com.example.btms.desktop.controller.cateoftuornament;

import com.example.btms.config.Prefs;
import com.example.btms.desktop.controller.category.NoiDungController;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.cateoftuornament.ChiTietGiaiDau;
import com.example.btms.model.tournament.GiaiDau;
import com.example.btms.service.cateoftuornament.ChiTietGiaiDauService;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.tournament.GiaiDauService;
import com.example.btms.ui.cateoftuornament.DangKyNoiDungPanel;

import javax.swing.*;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.table.TableRowSorter;

public class DangKyNoiDungController {

    private final DangKyNoiDungPanel view;
    private final NoiDungService noiDungService;
    private final ChiTietGiaiDauService chiTietService;
    private final Prefs prefs;
    private final GiaiDauService giaiDauService; // nullable

    private boolean showAll = false; // filter toggle
    private boolean updating = false; // tránh đệ quy khi rollback checkbox

    public DangKyNoiDungController(
            DangKyNoiDungPanel view,
            NoiDungService noiDungService,
            ChiTietGiaiDauService chiTietService,
            Prefs prefs,
            GiaiDauService giaiDauService // có thể null
    ) {
        this.view = Objects.requireNonNull(view);
        this.noiDungService = Objects.requireNonNull(noiDungService);
        this.chiTietService = Objects.requireNonNull(chiTietService);
        this.prefs = Objects.requireNonNull(prefs);
        this.giaiDauService = giaiDauService;

        wire();
        reloadAsync();
    }

    private void wire() {
        view.getBtnRefresh().addActionListener(e -> reloadAsync());
        view.getBtnShowAll().addActionListener(e -> {
            showAll = !showAll;
            view.getBtnShowAll().setText(showAll ? "Chỉ đã đăng ký" : "Hiện tất cả");
            updateFilters();
        });
        view.getBtnAdd().addActionListener(e -> onAddNoiDung());
        view.getBtnEdit().addActionListener(e -> onEditNoiDung());

        view.getTxtSearch().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void apply() {
                updateFilters();
            }

            public void insertUpdate(DocumentEvent e) {
                apply();
            }

            public void removeUpdate(DocumentEvent e) {
                apply();
            }

            public void changedUpdate(DocumentEvent e) {
                apply();
            }
        });

        view.getModel().addTableModelListener(e -> {
            if (e.getColumn() != 0 || e.getFirstRow() < 0)
                return;
            if (updating)
                return;
            handleToggle(e.getFirstRow());
        });
    }

    // ---------- Public APIs ----------
    public void refreshAll() {
        reloadAsync();
    }

    public void selectNoiDungById(Integer idNoiDung) {
        if (idNoiDung == null)
            return;
        if (!showAll) {
            showAll = true;
            view.getBtnShowAll().setText("Chỉ đã đăng ký");
        }
        if (!view.getTxtSearch().getText().isBlank())
            view.getTxtSearch().setText("");
        updateFilters();

        DefaultTableModel model = view.getModel();
        int target = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object v = model.getValueAt(i, 1);
            if (v instanceof Integer iid && iid.equals(idNoiDung)) {
                target = i;
                break;
            }
        }
        if (target >= 0) {
            int vr = view.getTable().convertRowIndexToView(target);
            if (vr >= 0) {
                view.getTable().getSelectionModel().setSelectionInterval(vr, vr);
                view.getTable().scrollRectToVisible(view.getTable().getCellRect(vr, 0, true));
            }
        }
    }

    // ---------- Core ----------
    private void reloadAsync() {
        SwingUtilities.invokeLater(() -> {
            int idGiai = prefs.getInt("selectedGiaiDauId", -1);
            String tenGiai = prefs.get("selectedGiaiDauName", null);

            if (idGiai <= 0) {
                view.getLblGiaiInfo().setText("Giải đã chọn: (chưa chọn)");
                view.getModel().setRowCount(0);
                updateCountLabel();
                return;
            }
            if ((tenGiai == null || tenGiai.isBlank()) && giaiDauService != null) {
                try {
                    Optional<GiaiDau> opt = giaiDauService.getGiaiDauById(idGiai);
                    if (opt.isPresent())
                        tenGiai = opt.get().getTenGiai();
                } catch (SQLException ignore) {
                }
            }
            view.getLblGiaiInfo().setText(
                    (tenGiai != null && !tenGiai.isBlank()) ? ("Nội dung thi đấu của giải: " + tenGiai)
                            : ("Giải đã chọn: ID=" + idGiai));

            try {
                List<NoiDung> list = noiDungService.getAllNoiDung();
                DefaultTableModel m = view.getModel();
                m.setRowCount(0);
                for (NoiDung nd : list) {
                    boolean registered = chiTietService.exists(idGiai, nd.getId());
                    String gt = Optional.ofNullable(nd.getGioiTinh()).orElse("").trim();
                    String genderText = "f".equalsIgnoreCase(gt) ? "Nữ" : "m".equalsIgnoreCase(gt) ? "Nam" : "Nam, Nữ";
                    String teamText = Boolean.TRUE.equals(nd.getTeam()) ? "Đôi" : "Đơn";
                    m.addRow(new Object[] { registered, nd.getId(), nd.getTenNoiDung(),
                            nd.getTuoiDuoi(), nd.getTuoiTren(), genderText, teamText });
                }
                updateFilters();
                updateCountLabel();
            } catch (SQLException ex) {
                view.err("Lỗi tải dữ liệu: " + ex.getMessage());
            }
        });
    }

    private void updateFilters() {
        TableRowSorter<DefaultTableModel> sorter = view.getSorter();
        if (sorter == null)
            return;

        try {
            String q = view.getTxtSearch().getText();
            RowFilter<DefaultTableModel, Integer> searchFilter = null;
            if (q != null && !q.trim().isEmpty()) {
                String pattern = Pattern.quote(q.trim());
                searchFilter = RowFilter.regexFilter("(?i)" + pattern, 2); // cột 2: "Nội dung"
            }
            RowFilter<DefaultTableModel, Integer> onlyRegistered = new RowFilter<>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                    if (showAll)
                        return true;
                    return Boolean.TRUE.equals(e.getValue(0));
                }
            };
            List<RowFilter<DefaultTableModel, Integer>> fs = new ArrayList<>();
            fs.add(onlyRegistered);
            if (searchFilter != null)
                fs.add(searchFilter);
            sorter.setRowFilter(RowFilter.andFilter(fs));
        } catch (Exception ignore) {
            sorter.setRowFilter(showAll ? null : new RowFilter<>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                    return Boolean.TRUE.equals(e.getValue(0));
                }
            });
        }
        updateCountLabel();
    }

    private void handleToggle(int modelRow) {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (idGiai <= 0) {
            view.err("Chưa chọn giải đấu.");
            reloadAsync();
            return;
        }
        DefaultTableModel m = view.getModel();
        Boolean checked = (Boolean) m.getValueAt(modelRow, 0);
        Integer idNoiDung = (Integer) m.getValueAt(modelRow, 1);
        Integer tuoiDuoi = (Integer) m.getValueAt(modelRow, 3);
        Integer tuoiTren = (Integer) m.getValueAt(modelRow, 4);

        try {
            boolean exists = chiTietService.exists(idGiai, idNoiDung);
            if (Boolean.TRUE.equals(checked)) {
                if (!exists) {
                    int td = (tuoiDuoi == null) ? 0 : tuoiDuoi;
                    int tt = (tuoiTren == null) ? 0 : tuoiTren;
                    chiTietService.create(new ChiTietGiaiDau(idGiai, idNoiDung, td, tt));
                }
            } else {
                if (exists)
                    chiTietService.delete(idGiai, idNoiDung);
            }
            updateCountLabel();
        } catch (RuntimeException ex) {
            view.err("Không thể cập nhật đăng ký: " + ex.getMessage());
            // rollback checkbox
            updating = true;
            try {
                m.setValueAt(!Boolean.TRUE.equals(checked), modelRow, 0);
            } finally {
                updating = false;
            }
            updateCountLabel();
        }
    }

    private void updateCountLabel() {
        int visible = view.getTable().getRowCount();
        int totalRegistered = 0;
        DefaultTableModel m = view.getModel();
        for (int i = 0; i < m.getRowCount(); i++) {
            if (Boolean.TRUE.equals(m.getValueAt(i, 0)))
                totalRegistered++;
        }
        view.getLblCount().setText(visible + "/" + totalRegistered + " nội dung");
    }

    // ---------- Add / Edit ----------
    private void onAddNoiDung() {
        var ctrl = new NoiDungController(
                SwingUtilities.getWindowAncestor(view),
                "Thêm nội dung",
                null,
                noiDungService);
        ctrl.open();
        if (ctrl.isSaved()) {
            reloadAsync();
            // Hỏi đăng ký nội dung vừa thêm cho giải hiện tại
            int idGiai = prefs.getInt("selectedGiaiDauId", -1);
            if (idGiai > 0) {
                int choice = JOptionPane.showConfirmDialog(
                        view,
                        "Bạn có muốn đăng ký nội dung vừa thêm cho giải hiện tại không?",
                        "Đăng ký nội dung",
                        JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    NoiDung nd = ctrl.getResult();
                    if (nd != null && nd.getId() != null) {
                        try {
                            int td = nd.getTuoiDuoi() == null ? 0 : nd.getTuoiDuoi();
                            int tt = nd.getTuoiTren() == null ? 0 : nd.getTuoiTren();
                            chiTietService.create(new ChiTietGiaiDau(idGiai, nd.getId(), td, tt));
                            reloadAsync();
                        } catch (RuntimeException ex) {
                            view.err("Không thể đăng ký: " + ex.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void onEditNoiDung() {
        int vr = view.getTable().getSelectedRow();
        if (vr < 0) {
            view.info("Vui lòng chọn một dòng để sửa.");
            return;
        }
        int mr = view.getTable().convertRowIndexToModel(vr);
        Integer id = (Integer) view.getModel().getValueAt(mr, 1);

        try {
            Optional<NoiDung> ndOpt = noiDungService.getNoiDungById(id);
            if (ndOpt.isEmpty()) {
                view.info("Không tìm thấy nội dung.");
                return;
            }

            var ctrl = new NoiDungController(
                    SwingUtilities.getWindowAncestor(view),
                    "Sửa nội dung",
                    ndOpt.get(),
                    noiDungService);
            ctrl.open();
            if (ctrl.isSaved())
                reloadAsync();
        } catch (SQLException ex) {
            view.err("Lỗi: " + ex.getMessage());
        }
    }
}