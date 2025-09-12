package com.example.badmintoneventtechnology.model.tournament;

public class ViTri {
    private int vitriId;
    private int bocThamId;
    private int soThuTu;
    private Integer dangkyId;
    private Integer hatGiong;
    private boolean bye;

    public ViTri() {}

    public ViTri(int bocThamId, int soThuTu, Integer dangkyId, Integer hatGiong, boolean bye) {
        this.bocThamId = bocThamId;
        this.soThuTu = soThuTu;
        this.dangkyId = dangkyId;
        this.hatGiong = hatGiong;
        this.bye = bye;
    }

    // Getters and Setters
    public int getVitriId() { return vitriId; }
    public void setVitriId(int vitriId) { this.vitriId = vitriId; }

    public int getBocThamId() { return bocThamId; }
    public void setBocThamId(int bocThamId) { this.bocThamId = bocThamId; }

    public int getSoThuTu() { return soThuTu; }
    public void setSoThuTu(int soThuTu) { this.soThuTu = soThuTu; }

    public Integer getDangkyId() { return dangkyId; }
    public void setDangkyId(Integer dangkyId) { this.dangkyId = dangkyId; }

    public Integer getHatGiong() { return hatGiong; }
    public void setHatGiong(Integer hatGiong) { this.hatGiong = hatGiong; }

    public boolean isBye() { return bye; }
    public void setBye(boolean bye) { this.bye = bye; }

    @Override
    public String toString() {
        return "Vị trí " + soThuTu + (bye ? " (Bye)" : "");
    }
}
