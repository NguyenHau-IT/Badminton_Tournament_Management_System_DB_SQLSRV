package com.example.btms.desktop.controller.category;

import com.example.btms.config.Prefs;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.team.ChiTietDoi;
import com.example.btms.model.team.DangKiDoi;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.player.DangKiCaNhanService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.service.team.ChiTietDoiService;
import com.example.btms.service.team.DangKiDoiService;
import com.example.btms.ui.category.ChangeCategoryTransferDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;
import java.util.Map.Entry;

public class ChangeCategoryTransferController {

    private final ChangeCategoryTransferDialog view;
    private final Prefs prefs;
    private final boolean teamMode;
    private final DangKiCaNhanService dkCaNhanService; // nullable if teamMode=true
    private final VanDongVienService vdvService;
    private final DangKiDoiService doiService; // nullable if teamMode=false
    private final ChiTietDoiService chiTietDoiService; // nullable if teamMode=false
    private final CauLacBoService clbService;
    private final Runnable onChanged;

    public ChangeCategoryTransferController(
            ChangeCategoryTransferDialog view,
            Prefs prefs,
            boolean teamMode,
            // singles
            DangKiCaNhanService dkCaNhanService,
            VanDongVienService vdvService,
            // teams
            DangKiDoiService doiService,
            ChiTietDoiService chiTietDoiService,
            CauLacBoService clbService,
            Runnable onChanged) {
        this.view = Objects.requireNonNull(view);
        this.prefs = Objects.requireNonNull(prefs);
        this.teamMode = teamMode;
        this.dkCaNhanService = dkCaNhanService;
        this.vdvService = vdvService;
        this.doiService = doiService;
        this.chiTietDoiService = chiTietDoiService;
        this.clbService = clbService;
        this.onChanged = (onChanged != null) ? onChanged : () -> {
        };

        wire();
        // load initial
        reloadLeftAsync();
        reloadRightAsync();
    }

    private void wire() {
        view.getCboLeft().addActionListener(e -> reloadLeftAsync());
        view.getCboRight().addActionListener(e -> reloadRightAsync());
        view.getBtnRefresh().addActionListener(e -> {
            reloadLeftAsync();
            reloadRightAsync();
        });
        view.getBtnClose().addActionListener(e -> view.dispose());
        view.getBtnToRight().addActionListener(e -> moveLeftToRightAsync());
    }

    // ---------- Async loads ----------
    private void reloadLeftAsync() {
        NoiDung left = (NoiDung) view.getCboLeft().getSelectedItem();
        runListLoader(left, view.getLeftModel(), true);
    }

    private void reloadRightAsync() {
        NoiDung right = (NoiDung) view.getCboRight().getSelectedItem();
        runListLoader(right, view.getRightModel(), false);
    }

    private void runListLoader(NoiDung nd, DefaultTableModel model, boolean isLeft) {
        model.setRowCount(0);
        if (nd == null)
            return;

        final int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (idGiai <= 0)
            return;

        view.setBusy(true);

        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() {
                List<Object[]> rows = new ArrayList<>();
                if (teamMode) {
                    // đội
                    List<DangKiDoi> teams = Objects.requireNonNull(doiService).listTeams(idGiai, nd.getId());
                    for (DangKiDoi t : teams) {
                        String clbName = "";
                        try {
                            var teamFull = doiService.getTeam(Objects.requireNonNull(t.getIdTeam()));
                            if (teamFull.getIdClb() != null) {
                                var clb = clbService.findOne(teamFull.getIdClb());
                                clbName = (clb != null) ? clb.getTenClb() : "";
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
                                        var v = vdvService.findOne(m.getIdVdv());
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
                        rows.add(new Object[] { t.getIdTeam(), t.getTenTeam(), clbName, members });
                    }
                } else {
                    // đơn
                    Map<String, Integer> map = Objects.requireNonNull(vdvService)
                            .loadSinglesNames(nd.getId(), idGiai);
                    for (Entry<String, Integer> e : map.entrySet()) {
                        String clbName = "";
                        try {
                            var v = vdvService.findOne(e.getValue());
                            if (v.getIdClb() != null) {
                                var clb = clbService.findOne(v.getIdClb());
                                clbName = (clb != null) ? clb.getTenClb() : "";
                            }
                        } catch (RuntimeException ignore) {
                        }
                        rows.add(new Object[] { e.getValue(), e.getKey(), clbName, "" });
                    }
                }
                return rows;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();
                    for (Object[] r : rows)
                        model.addRow(r);
                } catch (Exception ex) {
                    view.showErr((isLeft ? "Lỗi tải danh sách trái: " : "Lỗi tải danh sách phải: ") + ex.getMessage());
                } finally {
                    view.setBusy(false);
                }
            }
        }.execute();
    }

    // ---------- Move ----------
    private void moveLeftToRightAsync() {
        NoiDung left = (NoiDung) view.getCboLeft().getSelectedItem();
        NoiDung right = (NoiDung) view.getCboRight().getSelectedItem();

        if (left == null || right == null) {
            view.showInfo("Chọn nội dung trái và phải.");
            return;
        }
        if (Objects.equals(left.getId(), right.getId())) {
            view.showInfo("Hai nội dung đang trùng nhau.");
            return;
        }

        final int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (idGiai <= 0) {
            view.showInfo("Chưa chọn giải.");
            return;
        }

        int[] viewRows = view.getTblLeft().getSelectedRows();
        if (viewRows == null || viewRows.length == 0) {
            view.showInfo("Chọn dòng ở bảng bên trái để chuyển.");
            return;
        }

        // Lấy danh sách ID theo model index
        List<Integer> ids = new ArrayList<>();
        for (int vr : viewRows) {
            int mr = view.getTblLeft().convertRowIndexToModel(vr);
            Integer id = (Integer) view.getLeftModel().getValueAt(mr, 0); // cột 0 = ID
            ids.add(id);
        }

        view.setBusy(true);

        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() {
                int moved = 0, skipped = 0;
                for (Integer id : ids) {
                    try {
                        if (teamMode) {
                            Objects.requireNonNull(doiService).updateTeamCategory(id, right.getId());
                            moved++;
                        } else {
                            // tránh trùng
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
                return new int[] { moved, skipped };
            }

            @Override
            protected void done() {
                try {
                    int[] rs = get();
                    if (rs[0] > 0) {
                        reloadLeftAsync();
                        reloadRightAsync();
                        onChanged.run();
                    }
                    view.showInfo("Đã chuyển: " + rs[0] + (rs[1] > 0 ? ", bỏ qua: " + rs[1] : ""));
                } catch (Exception ex) {
                    view.showErr("Lỗi chuyển: " + ex.getMessage());
                } finally {
                    view.setBusy(false);
                }
            }
        }.execute();
    }
}
