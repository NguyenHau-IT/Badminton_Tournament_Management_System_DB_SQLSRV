package com.example.btms.desktop.controller.category;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.example.btms.config.Prefs;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.team.ChiTietDoi;
import com.example.btms.model.team.DangKiDoi;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.player.DangKiCaNhanService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.service.team.ChiTietDoiService;
import com.example.btms.service.team.DangKiDoiService;
import com.example.btms.ui.category.ChangeCategoryTransferDialog;
import com.example.btms.ui.category.ContentParticipantsPanel;
import com.example.btms.ui.team.DangKyDoiDialog;

public class ContentParticipantsController {

    private final ContentParticipantsPanel view;
    private final Prefs prefs;
    private final Connection conn;

    // Services
    private final NoiDungService noiDungService;
    private final VanDongVienService vdvService;
    private final DangKiCaNhanService dkCaNhanService;
    private final DangKiDoiService doiService;
    private final ChiTietDoiService chiTietDoiService;
    private final CauLacBoService clbService;

    public ContentParticipantsController(
            ContentParticipantsPanel view,
            Connection conn,
            Prefs prefs,
            NoiDungService noiDungService,
            VanDongVienService vdvService,
            DangKiCaNhanService dkCaNhanService,
            DangKiDoiService doiService,
            ChiTietDoiService chiTietDoiService,
            CauLacBoService clbService) {
        this.view = Objects.requireNonNull(view);
        this.conn = Objects.requireNonNull(conn);
        this.prefs = Objects.requireNonNull(prefs);
        this.noiDungService = Objects.requireNonNull(noiDungService);
        this.vdvService = Objects.requireNonNull(vdvService);
        this.dkCaNhanService = Objects.requireNonNull(dkCaNhanService);
        this.doiService = Objects.requireNonNull(doiService);
        this.chiTietDoiService = Objects.requireNonNull(chiTietDoiService);
        this.clbService = Objects.requireNonNull(clbService);

        wire();
        loadNoiDungComboAsync();
    }

    private void wire() {
        view.getBtnReload().addActionListener(e -> reloadParticipantsAsync());
        view.getBtnEditTeam().addActionListener(e -> onEditTeam());
        view.getBtnDelete().addActionListener(e -> onDelete());
        view.getBtnTransfer().addActionListener(e -> onOpenTransferDialog());

        view.getCboNoiDung().addActionListener(e -> {
            reloadParticipantsAsync();
            updateButtonsState();
        });

        view.getTable().getSelectionModel().addListSelectionListener(e -> updateButtonsState());

        view.getTxtSearch().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void apply() {
                String q = view.getTxtSearch().getText();
                if (q == null || q.isBlank()) {
                    view.getSorter().setRowFilter(null);
                } else {
                    view.getSorter()
                            .setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q.trim())));
                }
            }

            @Override
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

    // ---------- Load combo ND (chỉ ND đã đăng ký) ----------
    private void loadNoiDungComboAsync() {
        view.setBusy(true);
        new SwingWorker<List<NoiDung>, Void>() {
            @Override
            protected List<NoiDung> doInBackground() throws Exception {
                var repo = new NoiDungRepository(conn);
                Map<String, Integer>[] maps = repo.loadCategories(); // [0]=singles, [1]=doubles
                Set<Integer> allowedIds = new LinkedHashSet<>();
                allowedIds.addAll(maps[0].values());
                allowedIds.addAll(maps[1].values());

                List<NoiDung> all = noiDungService.getAllNoiDung();
                List<NoiDung> filtered = new ArrayList<>();
                for (NoiDung nd : all) {
                    if (nd.getId() != null && allowedIds.contains(nd.getId()))
                        filtered.add(nd);
                }
                filtered.sort(Comparator.comparing(NoiDung::getId));
                return filtered;
            }

            @Override
            protected void done() {
                try {
                    List<NoiDung> nds = get();
                    var model = new DefaultComboBoxModel<>(nds.toArray(NoiDung[]::new));
                    view.getCboNoiDung().setModel(model);
                    if (!nds.isEmpty())
                        view.getCboNoiDung().setSelectedIndex(0);
                    reloadParticipantsAsync();
                } catch (Exception ex) {
                    view.showErr("Lỗi tải nội dung: " + ex.getMessage());
                } finally {
                    view.setBusy(false);
                }
            }
        }.execute();
    }

    // ---------- Reload bảng ----------
    private void reloadParticipantsAsync() {
        DefaultTableModel tm = view.getModel();
        tm.setRowCount(0);

        NoiDung nd = (NoiDung) view.getCboNoiDung().getSelectedItem();
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);

        if (nd == null) {
            view.getLblInfo().setText("Chưa chọn nội dung");
            return;
        }
        if (idGiai <= 0) {
            view.getLblInfo().setText("Chưa chọn giải");
            return;
        }

        boolean isDoi = Boolean.TRUE.equals(nd.getTeam());
        String tenGiai = prefs.get("selectedGiaiDauName", "");
        String giaiLabel = (tenGiai != null && !tenGiai.isBlank()) ? tenGiai : ("Giải ID=" + idGiai);
        view.getLblInfo().setText((isDoi ? "ĐỘI" : "CÁ NHÂN") + " - " + giaiLabel);

        view.setBusy(true);
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() {
                List<Object[]> rows = new ArrayList<>();
                if (isDoi) {
                    List<DangKiDoi> teams = doiService.listTeams(idGiai, nd.getId());
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
                            List<ChiTietDoi> list = chiTietDoiService
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
                    Map<String, Integer> map = vdvService.loadSinglesNames(nd.getId(), idGiai);
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
                    for (Object[] r : get())
                        tm.addRow(r);
                } catch (Exception ex) {
                    view.showErr("Lỗi tải danh sách: " + ex.getMessage());
                } finally {
                    updateButtonsState();
                    view.setBusy(false);
                }
            }
        }.execute();
    }

    private void updateButtonsState() {
        NoiDung nd = (NoiDung) view.getCboNoiDung().getSelectedItem();
        boolean isDoi = nd != null && Boolean.TRUE.equals(nd.getTeam());
        boolean hasSelection = view.getTable().getSelectedRow() >= 0;
        view.getBtnEditTeam().setEnabled(isDoi && hasSelection);
        view.getBtnDelete().setEnabled(hasSelection);
        view.getBtnTransfer().setEnabled(true);
    }

    // ---------- Edit team ----------
    private void onEditTeam() {
        NoiDung nd = (NoiDung) view.getCboNoiDung().getSelectedItem();
        if (nd == null || !Boolean.TRUE.equals(nd.getTeam()))
            return;

        int viewRow = view.getTable().getSelectedRow();
        if (viewRow < 0) {
            view.showInfo("Chọn đội để sửa.");
            return;
        }

        int modelRow = view.getTable().convertRowIndexToModel(viewRow);
        Integer idTeam = (Integer) view.getModel().getValueAt(modelRow, 0);

        Integer idClbInit = null;
        String tenTeam = (String) view.getModel().getValueAt(modelRow, 1);
        try {
            var team = doiService.getTeam(idTeam);
            idClbInit = team.getIdClb();
            if (team.getTenTeam() != null)
                tenTeam = team.getTenTeam();
        } catch (RuntimeException ex) {
            System.err.println("Không lấy được đội: " + ex.getMessage());
        }

        Integer idVdv1Init = null, idVdv2Init = null;
        try {
            List<ChiTietDoi> members = chiTietDoiService.listMembers(idTeam);
            members.sort(Comparator.comparing(ChiTietDoi::getIdVdv));
            if (!members.isEmpty())
                idVdv1Init = members.get(0).getIdVdv();
            if (members.size() > 1)
                idVdv2Init = members.get(1).getIdVdv();
        } catch (RuntimeException ex) {
            System.err.println("Không lấy được thành viên đội: " + ex.getMessage());
        }

        // doubles list (để dialog khóa ND)
        List<NoiDung> doubles;
        try {
            doubles = loadRegisteredDoubles();
            if (doubles.isEmpty()) {
                view.showInfo("Chưa có nội dung ĐÔI đã đăng ký.");
                return;
            }
        } catch (java.sql.SQLException ex) {
            view.showErr("Lỗi tải nội dung: " + ex.getMessage());
            return;
        }

        var dlg = new DangKyDoiDialog(
                SwingUtilities.getWindowAncestor(view),
                conn,
                "Sửa đội",
                doiService,
                chiTietDoiService,
                vdvService,
                clbService,
                prefs.getInt("selectedGiaiDauId", -1),
                doubles,
                nd,
                idTeam,
                tenTeam,
                idClbInit,
                idVdv1Init,
                idVdv2Init);
        dlg.setVisible(true);
        reloadParticipantsAsync();
    }

    // ---------- Delete ----------
    private void onDelete() {
        int viewRow = view.getTable().getSelectedRow();
        if (viewRow < 0) {
            view.showInfo("Chọn một dòng để xóa.");
            return;
        }

        NoiDung nd = (NoiDung) view.getCboNoiDung().getSelectedItem();
        if (nd == null) {
            view.showInfo("Chưa chọn nội dung.");
            return;
        }

        int c = JOptionPane.showConfirmDialog(view, "Bạn có chắc muốn xóa đăng ký này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION)
            return;

        int modelRow = view.getTable().convertRowIndexToModel(viewRow);
        try {
            if (Boolean.TRUE.equals(nd.getTeam())) {
                Integer idTeam = (Integer) view.getModel().getValueAt(modelRow, 0);
                chiTietDoiService.removeAll(idTeam);
                doiService.deleteTeam(idTeam);
            } else {
                int idGiai = prefs.getInt("selectedGiaiDauId", -1);
                if (idGiai <= 0) {
                    view.showInfo("Chưa chọn giải.");
                    return;
                }
                Integer idVdv = (Integer) view.getModel().getValueAt(modelRow, 0);
                dkCaNhanService.delete(idGiai, nd.getId(), idVdv);
            }
            view.showInfo("Đã xóa thành công.");
            reloadParticipantsAsync();
        } catch (RuntimeException ex) {
            view.showErr("Xóa thất bại: " + ex.getMessage());
        }
    }

    // ---------- Open transfer dialog (re-use pair: ChangeCategoryTransferDialog +
    // Controller) ----------
    private void onOpenTransferDialog() {
        NoiDung curNd = (NoiDung) view.getCboNoiDung().getSelectedItem();
        if (curNd == null) {
            view.showInfo("Chưa chọn nội dung.");
            return;
        }

        boolean teamMode = Boolean.TRUE.equals(curNd.getTeam());
        try {
            List<NoiDung> list = teamMode ? loadRegisteredDoubles() : loadRegisteredSingles();
            if (list.isEmpty()) {
                view.showInfo(teamMode ? "Không có nội dung ĐÔI đã đăng ký." : "Không có nội dung ĐƠN đã đăng ký.");
                return;
            }

            var dlg = new ChangeCategoryTransferDialog(
                    SwingUtilities.getWindowAncestor(view),
                    teamMode,
                    list,
                    curNd);

            new ChangeCategoryTransferController(
                    dlg,
                    prefs,
                    teamMode,
                    // singles
                    dkCaNhanService,
                    vdvService,
                    // teams
                    doiService,
                    chiTietDoiService,
                    clbService,
                    this::reloadParticipantsAsync);

            dlg.setVisible(true);
        } catch (java.sql.SQLException ex) {
            view.showErr("Lỗi tải nội dung: " + ex.getMessage());
        }
    }

    // ---------- Helpers: loadRegisteredSingles/Doubles (giống panel cũ) ----------
    private List<NoiDung> loadRegisteredDoubles() throws java.sql.SQLException {
        var repo = new NoiDungRepository(conn);
        Map<String, Integer>[] maps = repo.loadCategories(); // [1]=doubles
        Set<Integer> ids = new LinkedHashSet<>(maps[1].values());
        List<NoiDung> all = noiDungService.getAllNoiDung();
        List<NoiDung> doubles = new ArrayList<>();
        for (NoiDung nd : all) {
            if (nd.getId() != null && ids.contains(nd.getId()) && Boolean.TRUE.equals(nd.getTeam()))
                doubles.add(nd);
        }
        doubles.sort(Comparator.comparing(NoiDung::getId));
        return doubles;
    }

    private List<NoiDung> loadRegisteredSingles() throws java.sql.SQLException {
        var repo = new NoiDungRepository(conn);
        Map<String, Integer>[] maps = repo.loadCategories(); // [0]=singles
        Set<Integer> ids = new LinkedHashSet<>(maps[0].values());
        List<NoiDung> all = noiDungService.getAllNoiDung();
        List<NoiDung> singles = new ArrayList<>();
        for (NoiDung nd : all) {
            if (nd.getId() != null && ids.contains(nd.getId()) && !Boolean.TRUE.equals(nd.getTeam()))
                singles.add(nd);
        }
        singles.sort(Comparator.comparing(NoiDung::getId));
        return singles;
    }

    public void refreshAll() {
        loadNoiDungComboAsync(); // sẽ set lại combo + tự reload bảng
    }

    // (tuỳ chọn) giữ API select theo ID như panel cũ
    public void selectNoiDungById(Integer idNoiDung) {
        if (idNoiDung == null)
            return;
        var combo = view.getCboNoiDung();
        var model = combo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            NoiDung nd = model.getElementAt(i);
            if (nd != null && idNoiDung.equals(nd.getId())) {
                combo.setSelectedIndex(i);
                // Không cần gọi reloadParticipantsAsync() ở đây vì ActionListener đã tự động
                // gọi
                return;
            }
        }
        // Chỉ reload nếu không tìm thấy idNoiDung trong combo
        reloadParticipantsAsync();
    }
}
