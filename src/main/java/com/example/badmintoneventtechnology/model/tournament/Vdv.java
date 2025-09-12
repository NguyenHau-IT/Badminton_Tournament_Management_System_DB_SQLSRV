package com.example.badmintoneventtechnology.model.tournament;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Vdv {
    private int vdvId;
    private String ho;
    private String ten;
    private String gioiTinh;
    private LocalDate ngaySinh;
    private Integer clbId;
    private String quocGia;
    private String sdt;
    private String email;
    private Integer xepHang;
    private LocalDateTime createdAt;

    public Vdv() {}

    public Vdv(String ho, String ten, String gioiTinh) {
        this.ho = ho;
        this.ten = ten;
        this.gioiTinh = gioiTinh;
        this.quocGia = "VN";
    }

    // Getters and Setters
    public int getVdvId() { return vdvId; }
    public void setVdvId(int vdvId) { this.vdvId = vdvId; }

    public String getHo() { return ho; }
    public void setHo(String ho) { this.ho = ho; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public Integer getClbId() { return clbId; }
    public void setClbId(Integer clbId) { this.clbId = clbId; }

    public String getQuocGia() { return quocGia; }
    public void setQuocGia(String quocGia) { this.quocGia = quocGia; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getXepHang() { return xepHang; }
    public void setXepHang(Integer xepHang) { this.xepHang = xepHang; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFullName() {
        return (ho != null ? ho + " " : "") + (ten != null ? ten : "");
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
