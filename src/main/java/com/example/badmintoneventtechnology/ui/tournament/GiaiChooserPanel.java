package com.example.badmintoneventtechnology.ui.tournament;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.example.badmintoneventtechnology.model.tournament.Giai;
import com.example.badmintoneventtechnology.service.db.DatabaseService;

public class GiaiChooserPanel extends JPanel {
    private JComboBox<Giai> cboGiai;
    private DatabaseService service;
    private Giai selectedGiai;
    private JTextArea txtThongTin;
    private JButton btnReload;

    public GiaiChooserPanel(DatabaseService service) {
        this.service = service;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Chọn giải đấu để tổ chức"));
        initUI();
        setupEventListeners();
    }

    private void initUI() {
        // Panel chọn giải
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Giải đấu:"));
        cboGiai = new JComboBox<>();
        cboGiai.setPreferredSize(new java.awt.Dimension(300, 25));
        topPanel.add(cboGiai);

        btnReload = new JButton("Làm mới");
        btnReload.addActionListener(e -> refreshData());
        topPanel.add(btnReload);

        add(topPanel, BorderLayout.NORTH);

        // Panel thông tin giải
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Thông tin giải đấu"));

        txtThongTin = new JTextArea(8, 40);
        txtThongTin.setEditable(false);
        txtThongTin.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txtThongTin.setText("Chưa chọn giải đấu nào...");

        JScrollPane scrollPane = new JScrollPane(txtThongTin);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void setupEventListeners() {
        cboGiai.addActionListener(e -> {
            selectedGiai = (Giai) cboGiai.getSelectedItem();
            updateThongTinGiai();
            // Fire property change event để thông báo cho các listener
            firePropertyChange("selectedGiai", null, selectedGiai);
        });
    }

    private void updateThongTinGiai() {
        if (selectedGiai != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("TÊN GIẢI: ").append(selectedGiai.getTen()).append("\n");
            sb.append("CẤP ĐỘ: ").append(selectedGiai.getCapDo()).append("\n");
            sb.append("ĐỊA ĐIỂM: ").append(selectedGiai.getDiaDiem()).append("\n");
            sb.append("THÀNH PHỐ: ").append(selectedGiai.getThanhPho()).append("\n");
            sb.append("NGÀY BẮT ĐẦU: ").append(selectedGiai.getNgayBd()).append("\n");
            sb.append("NGÀY KẾT THÚC: ").append(selectedGiai.getNgayKt()).append("\n");
            sb.append("\n");
            sb.append("Giải đấu này đã được chọn để tổ chức.\n");
            sb.append("Vui lòng chuyển sang tab 'Nội dung & VĐV' để xem chi tiết.");
            txtThongTin.setText(sb.toString());
        } else {
            txtThongTin.setText("Chưa chọn giải đấu nào...");
        }
    }

    private void loadGiaiList() {
        // Kiểm tra connection trước khi truy vấn
        if (!service.isConnected()) {
            return;
        }
        try {
            List<Giai> list = service.getAllGiai();
            cboGiai.removeAllItems();
            for (Giai g : list) {
                cboGiai.addItem(g);
            }
            if (cboGiai.getItemCount() > 0) {
                cboGiai.setSelectedIndex(0);
                selectedGiai = (Giai) cboGiai.getSelectedItem();
                updateThongTinGiai();
                // Fire property change event khi load xong data
                firePropertyChange("selectedGiai", null, selectedGiai);
            } else {
                selectedGiai = null;
                updateThongTinGiai();
            }
        } catch (Exception e) {
            // Xử lý lỗi một cách im lặng hoặc log
            System.err.println("Lỗi khi tải danh sách giải: " + e.getMessage());
        }
    }

    public Giai getSelectedGiai() {
        return selectedGiai;
    }

    public void reload() {
        loadGiaiList();
    }

    // Thêm method để load data từ bên ngoài khi đã kết nối DB
    public void refreshData() {
        loadGiaiList();
    }
}
