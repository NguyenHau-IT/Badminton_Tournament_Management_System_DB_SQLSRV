package com.example.badmintoneventtechnology.ui.tournament;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.example.badmintoneventtechnology.model.tournament.Giai;
import com.example.badmintoneventtechnology.service.db.DatabaseService;

public class GiaiChooserPanel extends JPanel {
    private final DatabaseService service;
    private Giai selectedGiai;
    private JPanel infoPanel;
    private JLabel lblTenGiai;
    private JLabel lblCapDo;
    private JLabel lblDiaDiem;
    private JLabel lblThanhPho;
    private JLabel lblNgayBd;
    private JLabel lblNgayKt;
    private JLabel lblTrangThai;
    private JButton btnChonGiai;
    private JButton btnDoiGiai;
    private JButton btnReload;
    private boolean locked = false; // Biến để kiểm tra trạng thái đã chọn giải hay chưa

    public GiaiChooserPanel(DatabaseService service) {
        this.service = service;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Chọn giải đấu để tổ chức"));
        initUI();
        setupEventListeners();
    }

    private void initUI() {
        // Sử dụng BorderLayout chính
        setLayout(new BorderLayout());

        // Panel chọn giải ở trên cùng
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        topPanel.add(new JLabel("Giải đấu:"));

        btnChonGiai = new JButton("Chọn giải đấu...");
        btnChonGiai.setPreferredSize(new java.awt.Dimension(150, 30));
        topPanel.add(btnChonGiai);

        btnDoiGiai = new JButton("Đổi giải");
        btnDoiGiai.setPreferredSize(new java.awt.Dimension(100, 30));
        btnDoiGiai.setVisible(false); // Ẩn ban đầu
        topPanel.add(btnDoiGiai);

        btnReload = new JButton("Làm mới");
        btnReload.addActionListener(e -> refreshData());
        topPanel.add(btnReload);

        add(topPanel, BorderLayout.NORTH);

        // Panel thông tin giải đấu ở phía dưới
        JPanel infoWrapperPanel = new JPanel(new BorderLayout());
        infoWrapperPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        createInfoPanel();

        // Đặt thông tin giải ở bên trái
        JPanel leftInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftInfoPanel.add(infoPanel);

        infoWrapperPanel.add(leftInfoPanel, BorderLayout.WEST);
        add(infoWrapperPanel, BorderLayout.CENTER);
    }

    private void createInfoPanel() {
        infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        infoPanel.setBackground(new Color(248, 248, 248));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 15);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tên giải - Header lớn và nổi bật
        lblTenGiai = new JLabel("Chưa chọn giải đấu");
        lblTenGiai.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        lblTenGiai.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 15, 0);
        infoPanel.add(lblTenGiai, gbc);

        // Reset insets và gridwidth cho các thông tin chi tiết
        gbc.insets = new Insets(3, 0, 3, 15);
        gbc.gridwidth = 1;

        // Cấp độ
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblCapDoLabel = new JLabel("🏆 Cấp độ:");
        lblCapDoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        infoPanel.add(lblCapDoLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        lblCapDo = new JLabel("-");
        lblCapDo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        infoPanel.add(lblCapDo, gbc);

        // Địa điểm
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblDiaDiemLabel = new JLabel("📍 Địa điểm:");
        lblDiaDiemLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        infoPanel.add(lblDiaDiemLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        lblDiaDiem = new JLabel("-");
        lblDiaDiem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        infoPanel.add(lblDiaDiem, gbc);

        // Thành phố
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblThanhPhoLabel = new JLabel("🌍 Thành phố:");
        lblThanhPhoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        infoPanel.add(lblThanhPhoLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        lblThanhPho = new JLabel("-");
        lblThanhPho.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        infoPanel.add(lblThanhPho, gbc);

        // Ngày bắt đầu
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblNgayBdLabel = new JLabel("📅 Bắt đầu:");
        lblNgayBdLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        infoPanel.add(lblNgayBdLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        lblNgayBd = new JLabel("-");
        lblNgayBd.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        infoPanel.add(lblNgayBd, gbc);

        // Ngày kết thúc
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblNgayKtLabel = new JLabel("📅 Kết thúc:");
        lblNgayKtLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        infoPanel.add(lblNgayKtLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        lblNgayKt = new JLabel("-");
        lblNgayKt.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        infoPanel.add(lblNgayKt, gbc);

        // Trạng thái
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 0, 0, 0);
        lblTrangThai = new JLabel("💡 Nhấn 'Chọn giải đấu...' để bắt đầu");
        lblTrangThai.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        lblTrangThai.setForeground(Color.GRAY);
        infoPanel.add(lblTrangThai, gbc);
    }

    private void setupEventListeners() {
        btnChonGiai.addActionListener(e -> {
            if (locked)
                return;
            showGiaiSelectionDialog();
        });

        btnDoiGiai.addActionListener(e -> {
            showGiaiSelectionDialog();
        });
    }

    private void showGiaiSelectionDialog() {
        // Kiểm tra connection trước khi truy vấn
        if (!service.isConnected()) {
            JOptionPane.showMessageDialog(this,
                    "Chưa kết nối database!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            List<Giai> list = service.getAllGiai();
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Không có giải đấu nào trong database!",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Tạo JList để hiển thị trong dialog
            JList<Giai> listGiai = new JList<>(list.toArray(new Giai[0]));
            listGiai.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            listGiai.setSelectedIndex(0);

            JScrollPane scrollPane = new JScrollPane(listGiai);
            scrollPane.setPreferredSize(new java.awt.Dimension(400, 200));

            int result = JOptionPane.showConfirmDialog(this,
                    scrollPane,
                    "Chọn giải đấu để tổ chức",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                Giai selected = listGiai.getSelectedValue();
                if (selected != null) {
                    selectedGiai = selected;
                    updateThongTinGiai();
                    // Fire property change event để thông báo cho các listener
                    firePropertyChange("selectedGiai", null, selectedGiai);

                    // Sau lần chọn đầu tiên, hiện nút đổi giải
                    if (!locked) {
                        btnChonGiai.setVisible(false);
                        btnDoiGiai.setVisible(true);
                        btnReload.setEnabled(false);
                        locked = true;
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi tải danh sách giải: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateThongTinGiai() {
        if (selectedGiai != null) {
            // Cập nhật tên giải với font lớn hơn và màu nổi bật
            lblTenGiai.setText(
                    selectedGiai.getTen() != null ? selectedGiai.getTen().toUpperCase() : "TÊN GIẢI KHÔNG XÁC ĐỊNH");
            lblTenGiai.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28)); // Font lớn hơn khi đã chọn
            lblTenGiai.setForeground(new Color(0, 128, 0)); // Màu xanh lá khi đã chọn

            // Cập nhật thông tin chi tiết
            lblCapDo.setText(selectedGiai.getCapDo() != null ? selectedGiai.getCapDo() : "Chưa xác định");
            lblDiaDiem.setText(selectedGiai.getDiaDiem() != null ? selectedGiai.getDiaDiem() : "Chưa xác định");
            lblThanhPho.setText(selectedGiai.getThanhPho() != null ? selectedGiai.getThanhPho() : "Chưa xác định");
            lblNgayBd.setText(selectedGiai.getNgayBd() != null ? selectedGiai.getNgayBd().toString() : "Chưa xác định");
            lblNgayKt.setText(selectedGiai.getNgayKt() != null ? selectedGiai.getNgayKt().toString() : "Chưa xác định");

            // Cập nhật trạng thái
            lblTrangThai.setText("✅ Đã chọn để tổ chức • Chuyển sang tab 'Nội dung & VĐV' để quản lý");
            lblTrangThai.setForeground(new Color(0, 128, 0));
        } else {
            // Reset về trạng thái ban đầu với font nhỏ hơn
            lblTenGiai.setText("Chưa chọn giải đấu");
            lblTenGiai.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24)); // Font mặc định
            lblTenGiai.setForeground(new Color(0, 102, 204));

            lblCapDo.setText("-");
            lblDiaDiem.setText("-");
            lblThanhPho.setText("-");
            lblNgayBd.setText("-");
            lblNgayKt.setText("-");

            lblTrangThai.setText("💡 Nhấn 'Chọn giải đấu...' để bắt đầu");
            lblTrangThai.setForeground(Color.GRAY);
        }

        // Refresh UI
        infoPanel.revalidate();
        infoPanel.repaint();
    }

    public Giai getSelectedGiai() {
        return selectedGiai;
    }

    public void reload() {
        // Không cần reload nữa vì dùng dialog
        refreshData();
    }

    // Thêm method để load data từ bên ngoài khi đã kết nối DB
    public void refreshData() {
        // Chỉ cần enable lại nút chọn giải nếu chưa lock
        if (!locked) {
            btnChonGiai.setEnabled(true);
        }
    }
}
