package com.example.btms.desktop.controller.club;

import com.example.btms.model.club.CauLacBo;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.ui.club.CauLacBoManagementPanel;

import javax.swing.*;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class CauLacBoManagementController {

    private final CauLacBoManagementPanel view;
    private final CauLacBoService service;

    public CauLacBoManagementController(CauLacBoManagementPanel view, CauLacBoService service) {
        this.view = Objects.requireNonNull(view);
        this.service = Objects.requireNonNull(service);
        wire();
        reload();
    }

    private void wire() {
        view.getBtnAdd().addActionListener(e -> onAdd());
        view.getBtnEdit().addActionListener(e -> onEdit());
        view.getBtnDelete().addActionListener(e -> onDelete());
        view.getBtnRefresh().addActionListener(e -> reload());

        view.getTxtSearch().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
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
    }

    // Public API (nếu bên ngoài muốn refresh)
    public void refreshAll() {
        reload();
    }

    private void reload() {
        try {
            List<CauLacBo> list = service.findAll();
            DefaultTableModel m = view.getModel();
            m.setRowCount(0);
            for (CauLacBo c : list) {
                m.addRow(new Object[] { c.getId(), c.getTenClb(), c.getTenNgan() });
            }
            updateFilter(); // re-apply filter + update count
        } catch (Exception ex) {
            view.err("Lỗi tải danh sách: " + ex.getMessage());
        }
    }

    private void updateFilter() {
        String q = view.getTxtSearch().getText();
        if (q == null || q.trim().isEmpty()) {
            view.getSorter().setRowFilter(null);
            updateCountLabel();
            return;
        }
        String pattern = java.util.regex.Pattern.quote(q.trim());
        RowFilter<DefaultTableModel, Integer> f1 = RowFilter.regexFilter("(?i)" + pattern, 1);
        RowFilter<DefaultTableModel, Integer> f2 = RowFilter.regexFilter("(?i)" + pattern, 2);
        view.getSorter().setRowFilter(RowFilter.orFilter(java.util.List.of(f1, f2)));
        updateCountLabel();
    }

    private void updateCountLabel() {
        int visible = view.getTable().getRowCount();
        int total = view.getModel().getRowCount();
        view.getLblCount().setText(visible + "/" + total + " câu lạc bộ");
    }

    private void onAdd() {
        var ctrl = new CauLacBoController(
                SwingUtilities.getWindowAncestor(view),
                "Thêm CLB",
                null, // original = null → thêm mới
                service);
        ctrl.open();
        if (ctrl.isSaved()) {
            reload();
        }
    }

    private void onEdit() {
        int vr = view.getTable().getSelectedRow();
        if (vr < 0) {
            view.warn("Vui lòng chọn CLB để sửa");
            return;
        }
        int mr = view.getTable().convertRowIndexToModel(vr);
        Integer id = (Integer) view.getModel().getValueAt(mr, 0);
        try {
            CauLacBo current = service.findOne(id);
            var ctrl = new CauLacBoController(
                    SwingUtilities.getWindowAncestor(view),
                    "Sửa CLB",
                    current,
                    service);
            ctrl.open();
            if (ctrl.isSaved()) {
                reload();
            }
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException ex) {
            view.err("Lỗi lấy thông tin: " + ex.getMessage());
        }
    }

    private void onDelete() {
        int vr = view.getTable().getSelectedRow();
        if (vr < 0) {
            view.warn("Vui lòng chọn CLB để xóa");
            return;
        }
        int mr = view.getTable().convertRowIndexToModel(vr);
        Integer id = (Integer) view.getModel().getValueAt(mr, 0);

        int confirm = JOptionPane.showConfirmDialog(view, "Bạn có chắc muốn xóa CLB này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        try {
            service.delete(id);
            view.info("Đã xóa CLB");
            reload();
        } catch (Exception ex) {
            view.err("Xóa thất bại: " + ex.getMessage());
        }
    }
}
