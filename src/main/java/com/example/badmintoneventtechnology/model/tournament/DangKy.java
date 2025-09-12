package com.example.badmintoneventtechnology.model.tournament;

import java.time.LocalDateTime;

public class DangKy {
    private int dangkyId;
    private int suKienId;
    private String loai;
    private Integer vdvId;
    private Integer capdoiId;
    private Integer hatGiong;
    private String trangThai;
    private LocalDateTime createdAt;

    public DangKy() {}

    public DangKy(int suKienId, String loai) {
        this.suKienId = suKienId;
        this.loai = loai;
        this.trangThai = "DUYET";
    }

    // Getters and Setters
    public int getDangkyId() { return dangkyId; }
    public void setDangkyId(int dangkyId) { this.dangkyId = dangkyId; }

    public int getSuKienId() { return suKienId; }
    public void setSuKienId(int suKienId) { this.suKienId = suKienId; }

    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public Integer getVdvId() { return vdvId; }
    public void setVdvId(Integer vdvId) { this.vdvId = vdvId; }

    public Integer getCapdoiId() { return capdoiId; }
    public void setCapdoiId(Integer capdoiId) { this.capdoiId = capdoiId; }

    public Integer getHatGiong() { return hatGiong; }
    public void setHatGiong(Integer hatGiong) { this.hatGiong = hatGiong; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Đăng ký #" + dangkyId + " (" + loai + ")";
    }
}
