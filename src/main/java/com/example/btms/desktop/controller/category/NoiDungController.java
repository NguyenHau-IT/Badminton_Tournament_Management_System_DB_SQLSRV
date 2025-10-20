package com.example.btms.desktop.controller.category;

import java.awt.Window;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.example.btms.model.category.NoiDung;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.ui.category.NoiDungDialog;

public class NoiDungController {

    private final NoiDungService noiDungService;
    private final NoiDung original; // null nếu tạo mới
    private final boolean isEditMode;
    private NoiDungDialog view;

    private boolean saved = false;
    private NoiDung result = null;

    public NoiDungController(Window parent, String title, NoiDung original, NoiDungService service) {
        this.noiDungService = Objects.requireNonNull(service);
        this.original = original;
        this.isEditMode = (original != null);
        this.view = new NoiDungDialog(parent, title, isEditMode);
        wire();
        if (isEditMode)
            view.fillFrom(original);
    }

    private void wire() {
        view.getBtnSave().addActionListener(e -> onSave());
        view.getBtnCancel().addActionListener(e -> view.dispose());
    }

    public void open() {
        view.setVisible(true);
    }

    public boolean isSaved() {
        return saved;
    }

    public NoiDung getResult() {
        return result;
    }

    // ---------------- save logic ----------------
    private void onSave() {
        String tenNoiDung = view.getTenNoiDungField().getText().trim();
        if (tenNoiDung.isEmpty()) {
            view.error("Tên nội dung không được để trống!");
            return;
        }

        int tuoiDuoi, tuoiTren;
        try {
            tuoiDuoi = Integer.parseInt(view.getTuoiDuoiField().getText().trim());
            tuoiTren = Integer.parseInt(view.getTuoiTrenField().getText().trim());
        } catch (NumberFormatException nfe) {
            view.error("Tuổi dưới/trên phải là số nguyên hợp lệ!");
            return;
        }

        final String gioiTinh = ((String) view.getGioiTinhCombo().getSelectedItem() != null)
                ? ((String) view.getGioiTinhCombo().getSelectedItem()).trim()
                : null;
        boolean team = view.getTeamCheckBox().isSelected();

        view.setBusy(true);
        new SwingWorker<Boolean, Void>() {
            private NoiDung out;
            private String err;

            @Override
            protected Boolean doInBackground() {
                try {
                    if (isEditMode) {
                        original.setTenNoiDung(tenNoiDung);
                        original.setTuoiDuoi(tuoiDuoi);
                        original.setTuoiTren(tuoiTren);
                        original.setGioiTinh(gioiTinh);
                        original.setTeam(team);
                        boolean ok = noiDungService.updateNoiDung(original);
                        if (ok)
                            out = original;
                        return ok;
                    } else {
                        NoiDung newNd = new NoiDung(null, tenNoiDung, tuoiDuoi, tuoiTren, gioiTinh, team);
                        out = noiDungService.createNoiDung(newNd);
                        return out != null;
                    }
                } catch (IllegalArgumentException iae) {
                    err = iae.getMessage();
                    return false;
                } catch (SQLException | RuntimeException se) {
                    err = se.getMessage() != null ? se.getMessage()
                            : (se.getCause() != null ? se.getCause().getMessage() : se.getClass().getSimpleName());
                    return false;
                }
            }

            @Override
            protected void done() {
                view.setBusy(false);
                try {
                    boolean ok = get();
                    if (ok) {
                        saved = true;
                        result = out;
                        view.info(isEditMode ? "Cập nhật nội dung thành công!" : "Thêm nội dung thành công!");
                        view.dispose();
                    } else {
                        view.error(err != null ? err
                                : (isEditMode ? "Không thể cập nhật nội dung!" : "Không thể thêm nội dung!"));
                    }
                } catch (InterruptedException | ExecutionException e) {
                    view.error(e.getMessage());
                }
            }
        }.execute();
    }
}
