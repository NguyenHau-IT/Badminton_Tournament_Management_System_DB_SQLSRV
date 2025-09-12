package com.example.badmintoneventtechnology.model.tournament;

import java.time.LocalDateTime;

public class SuKien {
    private int suKienId;
    private int giaiId;
    private String ma;
    private String ten;
    private String nhomTuoi;
    private String trinhDo;
    private int soLuong;
    private String luatThiDau;
    private String loaiBang;
    private LocalDateTime createdAt;

    public SuKien() {}

    public SuKien(int giaiId, String ma, String ten) {
        this.giaiId = giaiId;
        this.ma = ma;
        this.ten = ten;
        this.soLuong = 64;
        this.luatThiDau = "3x21 rally";
        this.loaiBang = "LOAI_TRUC_TIEP";
    }

    // Getters and Setters
    public int getSuKienId() { return suKienId; }
    public void setSuKienId(int suKienId) { this.suKienId = suKienId; }

    public int getGiaiId() { return giaiId; }
    public void setGiaiId(int giaiId) { this.giaiId = giaiId; }

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getNhomTuoi() { return nhomTuoi; }
    public void setNhomTuoi(String nhomTuoi) { this.nhomTuoi = nhomTuoi; }

    public String getTrinhDo() { return trinhDo; }
    public void setTrinhDo(String trinhDo) { this.trinhDo = trinhDo; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public String getLuatThiDau() { return luatThiDau; }
    public void setLuatThiDau(String luatThiDau) { this.luatThiDau = luatThiDau; }

    public String getLoaiBang() { return loaiBang; }
    public void setLoaiBang(String loaiBang) { this.loaiBang = loaiBang; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return ten + " (" + ma + ")";
    }
}
