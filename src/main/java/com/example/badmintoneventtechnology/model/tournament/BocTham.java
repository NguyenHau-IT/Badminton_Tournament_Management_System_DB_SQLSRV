package com.example.badmintoneventtechnology.model.tournament;

import java.time.LocalDateTime;

public class BocTham {
    private int bocThamId;
    private int suKienId;
    private String ten;
    private String kieu;
    private LocalDateTime createdAt;

    public BocTham() {}

    public BocTham(int suKienId, String ten, String kieu) {
        this.suKienId = suKienId;
        this.ten = ten;
        this.kieu = kieu;
    }

    // Getters and Setters
    public int getBocThamId() { return bocThamId; }
    public void setBocThamId(int bocThamId) { this.bocThamId = bocThamId; }

    public int getSuKienId() { return suKienId; }
    public void setSuKienId(int suKienId) { this.suKienId = suKienId; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getKieu() { return kieu; }
    public void setKieu(String kieu) { this.kieu = kieu; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return ten + " (" + kieu + ")";
    }
}
