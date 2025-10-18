package com.example.btms.desktop.controller.club;

import com.example.btms.model.club.CauLacBo;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.ui.club.CauLacBoDialog;

import javax.swing.*;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class CauLacBoController {

    private final CauLacBoService service;
    private final CauLacBo original; // null nếu tạo mới
    private final boolean editMode;
    private final CauLacBoDialog view;

    private boolean saved = false;

    public CauLacBoController(Window parent, String title, CauLacBo original, CauLacBoService service) {
        this.service = Objects.requireNonNull(service);
        this.original = original;
        this.editMode = original != null;
        this.view = new CauLacBoDialog(parent, title, editMode);
        wire();
        if (editMode)
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

    // ---------------- Save logic ----------------
    private void onSave() {
        String ten = view.getTenField().getText() == null ? "" : view.getTenField().getText().trim();
        String tenNgan = view.getTenNganField().getText();
        if (tenNgan != null)
            tenNgan = tenNgan.trim();
        final String tenNganFinal = tenNgan;
        // validate như dialog cũ
        if (ten.isEmpty()) {
            view.warn("Vui lòng nhập Tên CLB.");
            view.getTenField().requestFocus();
            return;
        }
        if (ten.length() > 255) {
            view.warn("Tên CLB tối đa 255 ký tự.");
            view.getTenField().requestFocus();
            return;
        }
        if (tenNganFinal != null && !tenNganFinal.isEmpty() && tenNganFinal.length() > 100) {
            view.warn("Tên ngắn tối đa 100 ký tự.");
            view.getTenNganField().requestFocus();
            return;
        }

        view.setBusy(true);
        new SwingWorker<Boolean, Void>() {
            String err;

            @Override
            protected Boolean doInBackground() {
                try {
                    if (editMode) {
                        service.update(original.getId(), ten, tenNganFinal);
                        return true;
                    } else {
                        service.create(ten, tenNganFinal);
                        return true;
                    }
                } catch (IllegalArgumentException | IllegalStateException | java.util.NoSuchElementException ex) {
                    err = ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                view.setBusy(false);
                try {
                    if (get()) {
                        saved = true;
                        view.dispose();
                    } else {
                        view.err(err != null ? err : "Không thể lưu dữ liệu.");
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    view.err(ex.getMessage());
                }
            }
        }.execute();
    }
}
