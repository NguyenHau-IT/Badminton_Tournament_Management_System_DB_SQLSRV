package com.example.btms.ui.player;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.example.btms.config.Prefs;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.player.DangKyCaNhan;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.repository.player.VanDongVienRepository;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.player.DangKyCaNhanService;
import com.example.btms.service.player.VanDongVienService;

/**
 * Tab/quản lý đăng ký cá nhân (Singles) cho giải đang chọn.
 */
public class DangKyCaNhanPanel extends JPanel {
    private final Prefs prefs;
    private final NoiDungService noiDungService;
    private final DangKyCaNhanService dkService;
    private final VanDongVienService vdvService;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "ID_Nội dung", "Nội dung", "ID_VĐV", "VĐV" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) { return false; }
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0,1,3 -> Integer.class; // hidden ids
                default -> String.class;
            };
        }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    private final JLabel lblHeader = new JLabel("Đăng ký cá nhân: (chưa chọn giải)");
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JButton btnAdd = new JButton("Thêm");
    private final JButton btnEdit = new JButton("Sửa");
    private final JButton btnDelete = new JButton("Xóa");
    private final JLabel lblCount = new JLabel("0 đăng ký");
    private final javax.swing.JTextField txtSearch = new javax.swing.JTextField(14);
    private final JComboBox<String> cboFilterField = new JComboBox<>(new String[] { "Nội dung", "VĐV" });
    private final JComboBox<NoiDung> cboNoiDungFilter = new JComboBox<>();

    public DangKyCaNhanPanel(Connection conn) {
        this.prefs = new Prefs();
        this.noiDungService = new NoiDungService(new NoiDungRepository(conn));
        this.dkService = new DangKyCaNhanService(conn);
        this.vdvService = new VanDongVienService(new VanDongVienRepository(conn));

        setLayout(new BorderLayout(8,8));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);

        JPanel top = new JPanel(new BorderLayout());
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(lblHeader);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(btnRefresh);
    row2.add(new JLabel("Nội dung:"));
    row2.add(cboNoiDungFilter);
    row2.add(new JLabel("Lọc theo:"));
        row2.add(cboFilterField);
        row2.add(new JLabel("Tìm:"));
        row2.add(txtSearch);
        row2.add(lblCount);
        row2.add(btnAdd); row2.add(btnEdit); row2.add(btnDelete);
        top.add(row1, BorderLayout.NORTH);
        top.add(row2, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        hideIdColumns();

        btnRefresh.addActionListener(e -> reload());
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { updateFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { updateFilter(); }
        });
    cboFilterField.addActionListener(e -> updateFilter());
    cboNoiDungFilter.addActionListener(e -> updateFilter());

        reload();
    }

    private void reload() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        String tenGiai = prefs.get("selectedGiaiDauName", "");
        if (idGiai <= 0) {
            lblHeader.setText("Đăng ký cá nhân: (chưa chọn giải)");
            model.setRowCount(0);
            lblCount.setText("0 đăng ký");
            return;
        }
        lblHeader.setText("Đăng ký cá nhân cho giải: " + (tenGiai != null && !tenGiai.isBlank() ? tenGiai : ("ID="+idGiai)));
        model.setRowCount(0);
        try {
            // Load combobox nội dung filter (giữ lựa chọn cũ nếu có)
            NoiDung selectedNd = (NoiDung) cboNoiDungFilter.getSelectedItem();
            java.util.List<NoiDung> list = noiDungService.getAllNoiDung();
            java.util.List<NoiDung> singles = new java.util.ArrayList<>();
            for (NoiDung nd : list) if (!Boolean.TRUE.equals(nd.getTeam())) singles.add(nd);
            javax.swing.DefaultComboBoxModel<NoiDung> ndModel = new javax.swing.DefaultComboBoxModel<>(singles.toArray(NoiDung[]::new));
            cboNoiDungFilter.setModel(ndModel);
            if (selectedNd != null) {
                for (int i=0;i<cboNoiDungFilter.getItemCount();i++) {
                    if (cboNoiDungFilter.getItemAt(i).getId().equals(selectedNd.getId())) { cboNoiDungFilter.setSelectedIndex(i); break; }
                }
            }

            NoiDung filterNd = (NoiDung) cboNoiDungFilter.getSelectedItem();
            java.util.List<NoiDung> toLoad = new java.util.ArrayList<>();
            if (filterNd != null) toLoad.add(filterNd); else toLoad.addAll(singles);
            for (NoiDung nd : toLoad) {
                List<DangKyCaNhan> regs = dkService.listByGiaiAndNoiDung(idGiai, nd.getId());
                for (DangKyCaNhan r : regs) {
                    String tenVdv = vdvService.findAll().stream()
                        .filter(v -> v.getId().equals(r.getIdVdv()))
                        .map(v -> v.getHoTen())
                        .findFirst().orElse("");
                    model.addRow(new Object[]{ r.getId(), nd.getId(), nd.getTenNoiDung(), r.getIdVdv(), tenVdv });
                }
            }
            lblCount.setText(model.getRowCount() + " đăng ký");
            updateFilter();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải đăng ký: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFilter() {
        String q = txtSearch.getText();
        if (q == null || q.isBlank()) { sorter.setRowFilter(null); return; }
        int colIndex = "VĐV".equals(cboFilterField.getSelectedItem()) ? 4 : 2; // model indices after hiding
        RowFilter<DefaultTableModel, Object> textFilter = RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q.trim()), colIndex);
        NoiDung ndSel = (NoiDung) cboNoiDungFilter.getSelectedItem();
        if (ndSel == null) { sorter.setRowFilter(textFilter); return; }
        RowFilter<DefaultTableModel,Object> ndFilter = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                Integer idNd = (Integer) entry.getValue(1);
                return idNd != null && idNd.equals(ndSel.getId()) && textFilter.include(entry);
            }
        };
        sorter.setRowFilter(ndFilter);
    }

    private void onAdd() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (idGiai <= 0) { JOptionPane.showMessageDialog(this, "Chưa chọn giải."); return; }
        DangKyCaNhanDialog dlg = new DangKyCaNhanDialog(
            javax.swing.SwingUtilities.getWindowAncestor(this),
            "Thêm đăng ký cá nhân", idGiai,
            dkService, vdvService, noiDungService,
            null);
        dlg.setVisible(true);
        reload();
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn một dòng để sửa."); return; }
        int mRow = table.convertRowIndexToModel(row);
        Integer id = (Integer) model.getValueAt(mRow, 0);
        Optional<DangKyCaNhan> opt = dkService.find(id);
        if (opt.isEmpty()) { JOptionPane.showMessageDialog(this, "Không tìm thấy bản ghi."); return; }
        DangKyCaNhanDialog dlg = new DangKyCaNhanDialog(
            javax.swing.SwingUtilities.getWindowAncestor(this),
            "Sửa đăng ký cá nhân", prefs.getInt("selectedGiaiDauId", -1),
            dkService, vdvService, noiDungService,
            opt.get());
        dlg.setVisible(true);
        reload();
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn một dòng để xóa."); return; }
        int mRow = table.convertRowIndexToModel(row);
        Integer id = (Integer) model.getValueAt(mRow, 0);
        int c = JOptionPane.showConfirmDialog(this, "Xóa đăng ký này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            dkService.delete(id);
            reload();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Xóa thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hideIdColumns() {
        try {
            javax.swing.table.TableColumnModel columnModel = table.getColumnModel();
            java.util.List<String> hide = java.util.List.of("ID", "ID_Nội dung", "ID_VĐV");
            for (String h : hide) {
                int modelIndex = model.findColumn(h);
                if (modelIndex < 0) continue;
                int viewIndex = table.convertColumnIndexToView(modelIndex);
                if (viewIndex < 0) continue;
                javax.swing.table.TableColumn col = columnModel.getColumn(viewIndex);
                col.setMinWidth(0); col.setMaxWidth(0); col.setPreferredWidth(0);
            }
        } catch (Exception ignored) {}
    }
}
