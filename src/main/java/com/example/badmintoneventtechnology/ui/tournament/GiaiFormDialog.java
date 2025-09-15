package com.example.badmintoneventtechnology.ui.tournament;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

import com.example.badmintoneventtechnology.model.tournament.Giai;

public class GiaiFormDialog extends JDialog {
    private JTextField txtTen, txtDiaDiem;
    private JComboBox<String> cmbCapDo;
    private JComboBox<String> cmbThanhPho;
    private JSpinner spNgayBd, spNgayKt;
    private boolean ok = false;
    private final Giai giai;

    public GiaiFormDialog(Giai giai) {
        setModal(true);
        setTitle(giai == null ? "Thêm giải" : "Sửa giải");
        setSize(400, 300);
        setLocationRelativeTo(null);
        this.giai = giai;
        initUI();
        if (giai != null)
            fillForm(giai);
    }

    /**
     * Lấy danh sách tỉnh/thành phố từ API hoặc fallback list
     */
    private String[] getProvincesFromAPI() {
        List<String> provinces = new ArrayList<>();

        // Danh sách tỉnh/thành phố Việt Nam làm fallback
        String[] fallbackProvinces = {
                "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu", "Bắc Ninh",
                "Bến Tre", "Bình Định", "Bình Dương", "Bình Phước", "Bình Thuận", "Cà Mau",
                "Cao Bằng", "Đắk Lắk", "Đắk Nông", "Điện Biên", "Đồng Nai", "Đồng Tháp",
                "Gia Lai", "Hà Giang", "Hà Nam", "Hà Tĩnh", "Hải Dương", "Hậu Giang",
                "Hòa Bình", "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu",
                "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Long An", "Nam Định", "Nghệ An",
                "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Phú Yên", "Quảng Bình", "Quảng Nam",
                "Quảng Ngãi", "Quảng Ninh", "Quảng Trị", "Sóc Trăng", "Sơn La", "Tây Ninh",
                "Thái Bình", "Thái Nguyên", "Thanh Hóa", "Thừa Thiên Huế", "Tiền Giang",
                "Trà Vinh", "Tuyên Quang", "Vĩnh Long", "Vĩnh Phúc", "Yên Bái",
                "Hà Nội", "TP. Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ"
        };

        try {
            // Thử gọi API provinces.open-api.vn
            URL url = new URL("https://provinces.open-api.vn/api/p/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000); // 3 giây timeout
            connection.setReadTimeout(3000);

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON response đơn giản
                String jsonResponse = response.toString();
                if (jsonResponse.startsWith("[")) {
                    provinces.addAll(parseProvincesFromJSON(jsonResponse));
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể tải danh sách tỉnh/thành phố từ API: " + e.getMessage());
        }

        // Nếu không lấy được từ API, dùng danh sách fallback
        if (provinces.isEmpty()) {
            for (String province : fallbackProvinces) {
                provinces.add(province);
            }
            // Sắp xếp danh sách fallback theo thứ tự a-z
            Collections.sort(provinces);
        }

        return provinces.toArray(new String[0]);
    }

    /**
     * Parse đơn giản JSON response để lấy tên tỉnh/thành phố
     */
    private List<String> parseProvincesFromJSON(String json) {
        List<String> provinces = new ArrayList<>();
        try {
            // Parse JSON đơn giản (không dùng thư viện external)
            json = json.substring(1, json.length() - 1); // Bỏ [ ]
            String[] items = json.split("\\},\\{");

            for (String item : items) {
                item = item.replace("{", "").replace("}", "");
                String[] fields = item.split(",");

                for (String field : fields) {
                    if (field.contains("\"name\"")) {
                        String name = field.split(":")[1].trim();
                        name = name.replace("\"", "");
                        if (!name.isEmpty()) {
                            provinces.add(name);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse JSON: " + e.getMessage());
        }

        // Sắp xếp danh sách theo thứ tự a-z
        Collections.sort(provinces);

        return provinces;
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
        panel.setBorder(new javax.swing.border.EmptyBorder(12, 16, 12, 16)); // Tạo khoảng cách với viền
        panel.add(new JLabel("Tên giải:"));
        txtTen = new JTextField();
        panel.add(txtTen);
        panel.add(new JLabel("Cấp độ:"));
        cmbCapDo = new JComboBox<>(new String[] {
                "Mở rộng",
                "Cấp CLB",
                "Cấp xã",
                "Cấp tỉnh",
                "Cấp quốc gia"
        });
        cmbCapDo.setEditable(true); // Cho phép nhập tùy chỉnh
        panel.add(cmbCapDo);
        panel.add(new JLabel("Địa điểm:"));
        txtDiaDiem = new JTextField();
        panel.add(txtDiaDiem);
        panel.add(new JLabel("Tỉnh/Thành phố:"));
        cmbThanhPho = new JComboBox<>(getProvincesFromAPI());
        cmbThanhPho.setEditable(true); // Cho phép nhập tùy chỉnh
        panel.add(cmbThanhPho);
        panel.add(new JLabel("Ngày bắt đầu:"));
        spNgayBd = new JSpinner(new SpinnerDateModel());
        panel.add(spNgayBd);
        panel.add(new JLabel("Ngày kết thúc:"));
        spNgayKt = new JSpinner(new SpinnerDateModel());
        panel.add(spNgayKt);
        add(panel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOK = new JButton("OK");
        JButton btnCancel = new JButton("Huỷ");
        btnPanel.add(btnOK);
        btnPanel.add(btnCancel);
        add(btnPanel, BorderLayout.SOUTH);

        btnOK.addActionListener(e -> {
            ok = true;
            dispose();
        });
        btnCancel.addActionListener(e -> {
            ok = false;
            dispose();
        });
    }

    private void fillForm(Giai g) {
        txtTen.setText(g.getTen());
        cmbCapDo.setSelectedItem(g.getCapDo());
        txtDiaDiem.setText(g.getDiaDiem());
        cmbThanhPho.setSelectedItem(g.getThanhPho());
        // Ngày: cần chuyển LocalDate sang java.util.Date
        spNgayBd.setValue(java.sql.Date.valueOf(g.getNgayBd()));
        spNgayKt.setValue(java.sql.Date.valueOf(g.getNgayKt()));
    }

    public boolean showDialog() {
        setVisible(true);
        return ok;
    }

    public Giai getGiai() {
        Giai g = new Giai();
        g.setTen(txtTen.getText());
        g.setCapDo((String) cmbCapDo.getSelectedItem());
        g.setDiaDiem(txtDiaDiem.getText());
        g.setThanhPho((String) cmbThanhPho.getSelectedItem());
        java.util.Date bd = (java.util.Date) spNgayBd.getValue();
        java.util.Date kt = (java.util.Date) spNgayKt.getValue();
        g.setNgayBd(new java.sql.Date(bd.getTime()).toLocalDate());
        g.setNgayKt(new java.sql.Date(kt.getTime()).toLocalDate());
        return g;
    }
}
