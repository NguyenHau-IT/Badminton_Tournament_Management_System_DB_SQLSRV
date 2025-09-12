package com.example.badmintoneventtechnology.model.tournament;

import java.time.LocalDateTime;

public class Tran {
    private int tranId;
    private int bocThamId;
    private int vong;
    private int soTran;
    private int vitriTren;
    private int vitriDuoi;
    private Integer dangkyTrenId;
    private Integer dangkyDuoiId;
    private Integer thangId;
    private LocalDateTime gioBd;
    private Integer sanId;
    private String trangThai;

    public Tran() {}

    public Tran(int bocThamId, int vong, int soTran, int vitriTren, int vitriDuoi) {
        this.bocThamId = bocThamId;
        this.vong = vong;
        this.soTran = soTran;
        this.vitriTren = vitriTren;
        this.vitriDuoi = vitriDuoi;
        this.trangThai = "CHO";
    }

    // Getters and Setters
    public int getTranId() { return tranId; }
    public void setTranId(int tranId) { this.tranId = tranId; }

    public int getBocThamId() { return bocThamId; }
    public void setBocThamId(int bocThamId) { this.bocThamId = bocThamId; }

    public int getVong() { return vong; }
    public void setVong(int vong) { this.vong = vong; }

    public int getSoTran() { return soTran; }
    public void setSoTran(int soTran) { this.soTran = soTran; }

    public int getVitriTren() { return vitriTren; }
    public void setVitriTren(int vitriTren) { this.vitriTren = vitriTren; }

    public int getVitriDuoi() { return vitriDuoi; }
    public void setVitriDuoi(int vitriDuoi) { this.vitriDuoi = vitriDuoi; }

    public Integer getDangkyTrenId() { return dangkyTrenId; }
    public void setDangkyTrenId(Integer dangkyTrenId) { this.dangkyTrenId = dangkyTrenId; }

    public Integer getDangkyDuoiId() { return dangkyDuoiId; }
    public void setDangkyDuoiId(Integer dangkyDuoiId) { this.dangkyDuoiId = dangkyDuoiId; }

    public Integer getThangId() { return thangId; }
    public void setThangId(Integer thangId) { this.thangId = thangId; }

    public LocalDateTime getGioBd() { return gioBd; }
    public void setGioBd(LocalDateTime gioBd) { this.gioBd = gioBd; }

    public Integer getSanId() { return sanId; }
    public void setSanId(Integer sanId) { this.sanId = sanId; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return "Vòng " + vong + " - Trận " + soTran + " (" + trangThai + ")";
    }
}
