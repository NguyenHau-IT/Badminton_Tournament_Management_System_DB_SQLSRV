package com.example.badmintoneventtechnology.ui.cateoftuornament;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.example.badmintoneventtechnology.config.Prefs;
import com.example.badmintoneventtechnology.model.category.NoiDung;
import com.example.badmintoneventtechnology.model.cateoftuornament.ChiTietGiaiDau;
import com.example.badmintoneventtechnology.service.category.NoiDungService;
import com.example.badmintoneventtechnology.service.cateoftuornament.ChiTietGiaiDauService;

/**
 * Tab đăng ký nội dung cho giải đấu đã chọn (dựa theo Prefs.selectedGiaiDauId)
 */
public class DangKyNoiDungPanel extends JPanel {

    private final NoiDungService noiDungService;
    private final ChiTietGiaiDauService chiTietService;
    private final Prefs prefs;

    private final JLabel lblGiaiInfo = new JLabel("Giải đã chọn: (chưa chọn)");
    private final JButton btnRefresh = new JButton("Làm mới");

    private final DefaultTableModel model = new DefaultTableModel(new Object[] {
            "Đăng ký", "ID", "Nội dung", "Tuổi dưới", "Tuổi trên", "Giới tính", "Đánh đôi" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0; // chỉ cho phép tick cột checkbox
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0 -> Boolean.class;
                case 1 -> Integer.class;
                case 3, 4 -> Integer.class;
                default -> String.class;
            };
        }
    };

    private final JTable table = new JTable(model);
    private boolean updating = false; // tránh đệ quy khi rollback checkbox

    public DangKyNoiDungPanel(NoiDungService noiDungService, ChiTietGiaiDauService chiTietService, Prefs prefs) {
        this.noiDungService = Objects.requireNonNull(noiDungService);
        this.chiTietService = Objects.requireNonNull(chiTietService);
        this.prefs = Objects.requireNonNull(prefs);

        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.add(lblGiaiInfo);
        top.add(btnRefresh);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Sự kiện
        btnRefresh.addActionListener(e -> reloadAsync());
        table.getModel().addTableModelListener(e -> {
            if (e.getColumn() != 0 || e.getFirstRow() < 0)
                return;
            if (updating)
                return;
            int row = e.getFirstRow();
            handleToggle(row);
        });

        // Width nhẹ cho checkbox
        TableColumn checkCol = table.getColumnModel().getColumn(0);
        checkCol.setMaxWidth(90);
        checkCol.setMinWidth(80);

        reloadAsync();
    }

    private void reloadAsync() {
        SwingUtilities.invokeLater(() -> {
            Integer idGiai = prefs.getInt("selectedGiaiDauId", -1);
            if (idGiai == null || idGiai <= 0) {
                lblGiaiInfo.setText("Giải đã chọn: (chưa chọn)");
                model.setRowCount(0);
                JOptionPane.showMessageDialog(this, "Vui lòng chọn giải đấu trước.", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            lblGiaiInfo.setText("Giải đã chọn: ID=" + idGiai);

            try {
                List<NoiDung> list = noiDungService.getAllNoiDung();
                model.setRowCount(0);
                for (NoiDung nd : list) {
                    boolean registered = chiTietService.exists(idGiai, nd.getId());
                    model.addRow(new Object[] {
                            registered,
                            nd.getId(),
                            nd.getTenNoiDung(),
                            nd.getTuoiDuoi(),
                            nd.getTuoiTren(),
                            nd.getGioiTinh(),
                            Boolean.TRUE.equals(nd.getTeam()) ? "Đôi" : "Đơn"
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void handleToggle(int row) {
        Integer idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (idGiai == null || idGiai <= 0) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải đấu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            reloadAsync();
            return;
        }

        Boolean checked = (Boolean) model.getValueAt(row, 0);
        Integer idNoiDung = (Integer) model.getValueAt(row, 1);
        Integer tuoiDuoi = (Integer) model.getValueAt(row, 3);
        Integer tuoiTren = (Integer) model.getValueAt(row, 4);

        try {
            boolean exists = chiTietService.exists(idGiai, idNoiDung);
            if (Boolean.TRUE.equals(checked)) {
                if (!exists) {
                    ChiTietGiaiDau ct = new ChiTietGiaiDau(idGiai, idNoiDung,
                            tuoiDuoi != null ? tuoiDuoi : 0,
                            tuoiTren != null ? tuoiTren : 0);
                    chiTietService.create(ct);
                }
            } else {
                if (exists) {
                    chiTietService.delete(idGiai, idNoiDung);
                }
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật đăng ký: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            // rollback UI
            updating = true;
            try {
                model.setValueAt(!Boolean.TRUE.equals(checked), row, 0);
            } finally {
                updating = false;
            }
        }
    }
}
