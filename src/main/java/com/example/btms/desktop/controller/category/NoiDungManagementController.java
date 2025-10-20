package com.example.btms.desktop.controller.category;

import com.example.btms.model.category.NoiDung;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.ui.category.NoiDungManagementPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.RowFilter;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class NoiDungManagementController {

    private final NoiDungManagementPanel view;
    private final NoiDungService noiDungService;

    public NoiDungManagementController(NoiDungManagementPanel view, NoiDungService noiDungService) {
        this.view = Objects.requireNonNull(view);
        this.noiDungService = Objects.requireNonNull(noiDungService);
        wire();
        loadDataAsync();
    }

    private void wire() {
        view.getBtnRefresh().addActionListener(e -> loadDataAsync());
        view.getBtnAdd().addActionListener(e -> onAdd());
        view.getBtnEdit().addActionListener(e -> onEdit());
        view.getBtnDelete().addActionListener(e -> onDelete());

        // filter
        view.getTxtFilter().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void apply() {
                updateFilter();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                apply();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                apply();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                apply();
            }
        });
        view.getCmbColumn().addActionListener(e -> {
            if (!view.getTxtFilter().getText().trim().isEmpty())
                updateFilter();
            updateCountLabel();
        });
    }

    // -------- public API tương đương panel cũ --------
    public void refreshAll() {
        loadDataAsync();
    }

    // -------- data load ----------
    private void loadDataAsync() {
        DefaultTableModel tm = view.getTableModel();
        tm.setRowCount(0);
        view.setBusy(true);

        new SwingWorker<List<NoiDung>, Void>() {
            @Override
            protected List<NoiDung> doInBackground() throws Exception {
                return noiDungService.getAllNoiDung();
            }

            @Override
            protected void done() {
                try {
                    for (NoiDung nd : get()) {
                        String gt = Optional.ofNullable(nd.getGioiTinh()).orElse("").trim();
                        String genderText = "f".equalsIgnoreCase(gt) ? "Nữ"
                                : "m".equalsIgnoreCase(gt) ? "Nam" : "Nam, Nữ";
                        String teamText = Boolean.TRUE.equals(nd.getTeam()) ? "Đôi" : "Đơn";
                        tm.addRow(new Object[] {
                                nd.getId(), nd.getTenNoiDung(), nd.getTuoiDuoi(), nd.getTuoiTren(), genderText, teamText
                        });
                    }
                    updateCountLabel();
                } catch (Exception ex) {
                    view.err("Lỗi tải dữ liệu: " + ex.getMessage());
                } finally {
                    view.setBusy(false);
                }
            }
        }.execute();
    }

    private void updateFilter() {
        String text = view.getTxtFilter().getText();
        if (text == null || text.trim().isEmpty()) {
            view.getSorter().setRowFilter(null);
            updateCountLabel();
            return;
        }
        int selected = view.getCmbColumn().getSelectedIndex();
        if (selected < 0) {
            view.getSorter().setRowFilter(null);
            updateCountLabel();
            return;
        }
        int modelColumn = selected + 1; // bỏ cột ID
        try {
            String pattern = java.util.regex.Pattern.quote(text.trim());
            view.getSorter().setRowFilter(RowFilter.regexFilter("(?i)" + pattern, modelColumn));
        } catch (Exception ignore) {
            view.getSorter().setRowFilter(null);
        }
        updateCountLabel();
    }

    private void updateCountLabel() {
        int visible = view.getTable().getRowCount();
        int total = view.getTableModel().getRowCount();
        view.getLblCount().setText(visible + "/" + total + " nội dung");
    }

    // ---------- Add ----------
    private void onAdd() {
        var ctrl = new NoiDungController(
                SwingUtilities.getWindowAncestor(view),
                "Thêm nội dung",
                null,
                noiDungService);
        ctrl.open();
        if (ctrl.isSaved())
            loadDataAsync();
    }

    // ---------- Edit ----------
    private void onEdit() {
        int vr = view.getTable().getSelectedRow();
        if (vr < 0) {
            view.info("Vui lòng chọn nội dung để sửa");
            return;
        }
        int mr = view.getTable().convertRowIndexToModel(vr);
        Integer id = (Integer) view.getTableModel().getValueAt(mr, 0);

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
                loadDataAsync();
        } catch (SQLException e) {
            view.err("Lỗi lấy thông tin nội dung: " + e.getMessage());
        }
    }

    // ---------- Delete ----------
    private void onDelete() {
        int vr = view.getTable().getSelectedRow();
        if (vr < 0) {
            view.info("Vui lòng chọn nội dung để xóa");
            return;
        }
        int mr = view.getTable().convertRowIndexToModel(vr);
        Integer id = (Integer) view.getTableModel().getValueAt(mr, 0);

        int confirm = JOptionPane.showConfirmDialog(view, "Bạn có chắc muốn xóa nội dung này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        view.setBusy(true);
        new SwingWorker<Boolean, Void>() {
            String err;

            @Override
            protected Boolean doInBackground() {
                try {
                    return noiDungService.deleteNoiDung(id);
                } catch (SQLException e) {
                    err = e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                view.setBusy(false);
                try {
                    if (get()) {
                        view.info("Xóa nội dung thành công!");
                        loadDataAsync();
                    } else {
                        view.err(err != null ? err : "Không thể xóa nội dung");
                    }
                } catch (Exception ex) {
                    view.err(ex.getMessage());
                }
            }
        }.execute();
    }
}
