package com.example.btms.ui.draw;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.example.btms.config.Prefs;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.club.CauLacBo;
import com.example.btms.model.draw.BocThamDoi;
import com.example.btms.model.team.DangKiDoi;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.repository.club.CauLacBoRepository;
import com.example.btms.repository.draw.BocThamDoiRepository;
import com.example.btms.repository.team.DangKiDoiRepository;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.draw.BocThamDoiService;
import com.example.btms.service.team.DangKiDoiService;

/**
 * Trang bốc thăm đội (đôi) với thứ tự bắt đầu từ 0.
 * Cho phép:
 * - Chọn nội dung đôi
 * - Tải danh sách đội đã đăng ký nội dung đó
 * - Random thứ tự (bắt đầu 0)
 * - Kéo lên/xuống (swap) hoặc nhập lại thứ tự
 * - Lưu vào bảng BOC_THAM_DOI (reset & insert)
 */
public class BocThamDoiPanel extends JPanel {
    private final Prefs prefs = new Prefs();
    private final NoiDungService noiDungService;
    private final DangKiDoiService doiService;
    private final BocThamDoiService bocThamService;
    private final CauLacBoService clbService;
    private Map<Integer, String> clbNameCache = new HashMap<>();

    private final JComboBox<NoiDung> cboNoiDung = new JComboBox<>();
    private final JButton btnLoadTeams = new JButton("Nạp đội");
    private final JButton btnShuffle = new JButton("Bốc thăm ngẫu nhiên");
    private final JButton btnMoveUp = new JButton("Lên");
    private final JButton btnMoveDown = new JButton("Xuống");
    private final JButton btnSave = new JButton("Lưu vào DB");
    private final JButton btnReloadDraw = new JButton("Tải từ DB");

    private final JTextField txtFilter = new JTextField(14);

    // Cột: 0=Thứ tự, 1=Tên đội, 2=ID_CLB (ẩn), 3=CLB
    private final DefaultTableModel model = new DefaultTableModel(new Object[] { "Thứ tự", "Tên đội", "ID_CLB", "CLB" },
            0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return switch (c) {
                case 0, 2 -> Integer.class; // Thứ tự & ID_CLB
                default -> String.class;
            };
        }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    public BocThamDoiPanel(Connection conn) {
        Objects.requireNonNull(conn);
        this.noiDungService = new NoiDungService(new NoiDungRepository(conn));
        DangKiDoiRepository doiRepo = new DangKiDoiRepository(conn);
        this.doiService = new DangKiDoiService(doiRepo);
        this.bocThamService = new BocThamDoiService(conn, new BocThamDoiRepository(conn));
        this.clbService = new CauLacBoService(new CauLacBoRepository(conn));

        setLayout(new BorderLayout(8, 8));
        add(buildTop(), BorderLayout.NORTH);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);
        hideTechnicalColumns();
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        loadNoiDungOptions();
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        line.add(new JLabel("Nội dung:"));
        cboNoiDung.setPrototypeDisplayValue(new NoiDung(0, "XXXXXXXXXXXXXXXXXXXXX", null, null, null, true));
        line.add(cboNoiDung);
        line.add(btnLoadTeams);
        line.add(btnShuffle);
        line.add(btnMoveUp);
        line.add(btnMoveDown);
        line.add(btnSave);
        line.add(btnReloadDraw);
        line.add(new JLabel("Tìm:"));
        line.add(txtFilter);
        p.add(line, BorderLayout.CENTER);

        btnLoadTeams.addActionListener(e -> loadTeamsFromRegistration());
        btnShuffle.addActionListener(e -> shuffleCurrent());
        btnMoveUp.addActionListener(e -> moveSelected(-1));
        btnMoveDown.addActionListener(e -> moveSelected(1));
        btnSave.addActionListener(e -> saveDraw());
        btnReloadDraw.addActionListener(e -> loadDrawFromDb());
        cboNoiDung.addActionListener(e -> loadDrawFromDb());
        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        return p;
    }

    private void applyFilter() {
        String q = txtFilter.getText();
        if (q == null || q.isBlank()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q.trim()), 1));
    }

    private void loadNoiDungOptions() {
        try {
            List<NoiDung> all = noiDungService.getAllNoiDung();
            java.util.List<NoiDung> doubles = new java.util.ArrayList<>();
            for (NoiDung nd : all)
                if (Boolean.TRUE.equals(nd.getTeam()))
                    doubles.add(nd);
            cboNoiDung.removeAllItems();
            for (NoiDung nd : doubles)
                cboNoiDung.addItem(nd);
            if (cboNoiDung.getItemCount() > 0)
                cboNoiDung.setSelectedIndex(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nội dung: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private NoiDung selectedNd() {
        return (NoiDung) cboNoiDung.getSelectedItem();
    }

    private void loadTeamsFromRegistration() {
        NoiDung nd = selectedNd();
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (nd == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn nội dung.");
            return;
        }
        if (idGiai <= 0) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
            return;
        }
        try {
            List<DangKiDoi> teams = doiService.listTeams(idGiai, nd.getId());
            model.setRowCount(0);
            ensureClbCache();
            int idx = 0;
            for (DangKiDoi t : teams) {
                Integer idClb = t.getIdCauLacBo();
                if (idClb == null)
                    idClb = 0; // chuẩn hoá
                String clbName = clbNameCache.getOrDefault(idClb, idClb == 0 ? "" : ("CLB#" + idClb));
                model.addRow(new Object[] { idx++, t.getTenTeam(), idClb, clbName });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải đội: " + ex.getMessage());
        }
    }

    private void shuffleCurrent() {
        int rowCount = model.getRowCount();
        if (rowCount <= 1)
            return;
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            rows.add(new Object[] { model.getValueAt(i, 1), model.getValueAt(i, 2), model.getValueAt(i, 3) });
        }
        java.util.Collections.shuffle(rows, new Random());
        model.setRowCount(0);
        for (int i = 0; i < rows.size(); i++) {
            Object[] r = rows.get(i);
            model.addRow(new Object[] { i, r[0], r[1], r[2] });
        }
    }

    private void moveSelected(int delta) {
        int view = table.getSelectedRow();
        if (view < 0)
            return;
        int modelRow = table.convertRowIndexToModel(view);
        int target = modelRow + delta;
        if (target < 0 || target >= model.getRowCount())
            return;
        // hoán đổi toàn bộ dữ liệu (trừ cột thứ tự sẽ rebuild)
        Object ten1 = model.getValueAt(modelRow, 1);
        Object idClb1 = model.getValueAt(modelRow, 2);
        Object clb1 = model.getValueAt(modelRow, 3);
        Object ten2 = model.getValueAt(target, 1);
        Object idClb2 = model.getValueAt(target, 2);
        Object clb2 = model.getValueAt(target, 3);
        model.setValueAt(ten2, modelRow, 1);
        model.setValueAt(idClb2, modelRow, 2);
        model.setValueAt(clb2, modelRow, 3);
        model.setValueAt(ten1, target, 1);
        model.setValueAt(idClb1, target, 2);
        model.setValueAt(clb1, target, 3);
        // rebuild order
        for (int i = 0; i < model.getRowCount(); i++)
            model.setValueAt(i, i, 0);
        table.getSelectionModel().setSelectionInterval(table.convertRowIndexToView(target),
                table.convertRowIndexToView(target));
    }

    private void saveDraw() {
        NoiDung nd = selectedNd();
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (nd == null || idGiai <= 0) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải / nội dung.");
            return;
        }
        List<BocThamDoi> rows = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            int idClb = (Integer) model.getValueAt(i, 2);
            rows.add(new BocThamDoi(idGiai, nd.getId(), idClb, (String) model.getValueAt(i, 1), i, 1));
        }
        try {
            bocThamService.resetWithOrder(idGiai, nd.getId(), rows);
            JOptionPane.showMessageDialog(this, "Đã lưu bốc thăm (bắt đầu 0).", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lưu thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDrawFromDb() {
        NoiDung nd = selectedNd();
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (nd == null || idGiai <= 0) {
            return;
        }
        try {
            List<BocThamDoi> list = bocThamService.list(idGiai, nd.getId());
            model.setRowCount(0);
            ensureClbCache();
            for (BocThamDoi r : list) {
                int idClb = (r.getIdClb() != null) ? r.getIdClb() : 0;
                String clbName = clbNameCache.getOrDefault(idClb, idClb == 0 ? "" : ("CLB#" + idClb));
                model.addRow(new Object[] { r.getThuTu(), r.getTenTeam(), idClb, clbName });
            }
        } catch (RuntimeException ex) {
            // chưa có thì im lặng
        }
    }

    private void ensureClbCache() {
        if (!clbNameCache.isEmpty())
            return;
        try {
            for (CauLacBo c : clbService.findAll()) {
                if (c.getId() != null)
                    clbNameCache.put(c.getId(), c.getTenClb());
            }
        } catch (Exception ignored) {
        }
    }

    private void hideTechnicalColumns() {
        // Ẩn cột ID_CLB (index model 2)
        try {
            int viewIndex = table.convertColumnIndexToView(2);
            if (viewIndex >= 0) {
                var col = table.getColumnModel().getColumn(viewIndex);
                col.setMinWidth(0);
                col.setMaxWidth(0);
                col.setPreferredWidth(0);
            }
        } catch (Exception ignored) {
        }
    }
}
