package com.example.badmintoneventtechnology.model.tournament;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Giai {
    private int giaiId;
    private String ten;
    private String capDo;
    private String diaDiem;
    private String thanhPho;
    private LocalDate ngayBd;
    private LocalDate ngayKt;
    private LocalDateTime createdAt;

    public Giai() {}

    public Giai(String ten, String capDo, String diaDiem, String thanhPho, LocalDate ngayBd, LocalDate ngayKt) {
        this.ten = ten;
        this.capDo = capDo;
        this.diaDiem = diaDiem;
        this.thanhPho = thanhPho;
        this.ngayBd = ngayBd;
        this.ngayKt = ngayKt;
    }

    // Getters and Setters
    public int getGiaiId() { return giaiId; }
    public void setGiaiId(int giaiId) { this.giaiId = giaiId; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getCapDo() { return capDo; }
    public void setCapDo(String capDo) { this.capDo = capDo; }

    public String getDiaDiem() { return diaDiem; }
    public void setDiaDiem(String diaDiem) { this.diaDiem = diaDiem; }

    public String getThanhPho() { return thanhPho; }
    public void setThanhPho(String thanhPho) { this.thanhPho = thanhPho; }

    public LocalDate getNgayBd() { return ngayBd; }
    public void setNgayBd(LocalDate ngayBd) { this.ngayBd = ngayBd; }

    public LocalDate getNgayKt() { return ngayKt; }
    public void setNgayKt(LocalDate ngayKt) { this.ngayKt = ngayKt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return ten + " (" + capDo + ")";
    }
}
