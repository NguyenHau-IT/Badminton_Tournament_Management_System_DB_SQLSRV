package com.example.btms.model.category;

public class NoiDung {
    private Integer id;
    private String tenNoiDung;
    private Integer tuoiDuoi;
    private Integer tuoiTren;
    private String gioiTinh; // 'M', 'F', ...
    private Boolean team;

    public NoiDung() {
    }

    public NoiDung(Integer id, String tenNoiDung, Integer tuoiDuoi, Integer tuoiTren, String gioiTinh, Boolean team) {
        this.id = id;
        this.tenNoiDung = tenNoiDung;
        this.tuoiDuoi = tuoiDuoi;
        this.tuoiTren = tuoiTren;
        this.gioiTinh = gioiTinh;
        this.team = team;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenNoiDung() {
        return tenNoiDung;
    }

    public void setTenNoiDung(String tenNoiDung) {
        this.tenNoiDung = tenNoiDung;
    }

    public Integer getTuoiDuoi() {
        return tuoiDuoi;
    }

    public void setTuoiDuoi(Integer tuoiDuoi) {
        this.tuoiDuoi = tuoiDuoi;
    }

    public Integer getTuoiTren() {
        return tuoiTren;
    }

    public void setTuoiTren(Integer tuoiTren) {
        this.tuoiTren = tuoiTren;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public Boolean getTeam() {
        return team;
    }

    public void setTeam(Boolean team) {
        this.team = team;
    }

    @Override
    public String toString() {
        // Hiển thị tên nội dung để phù hợp với các combobox/JOptionPane lựa chọn
        // Nếu thiếu tên, fallback về "ND #id"
        return (tenNoiDung != null && !tenNoiDung.isBlank())
                ? tenNoiDung
                : (id != null ? ("ND #" + id) : "Nội dung");
    }
}
