package com.example.badmintoneventtechnology.model.tournament;

import java.time.LocalDateTime;

public class CapDoi {
    private int capdoiId;
    private int suKienId;
    private int vdv1Id;
    private int vdv2Id;
    private LocalDateTime createdAt;

    public CapDoi() {}

    public CapDoi(int suKienId, int vdv1Id, int vdv2Id) {
        this.suKienId = suKienId;
        this.vdv1Id = vdv1Id;
        this.vdv2Id = vdv2Id;
    }

    // Getters and Setters
    public int getCapdoiId() { return capdoiId; }
    public void setCapdoiId(int capdoiId) { this.capdoiId = capdoiId; }

    public int getSuKienId() { return suKienId; }
    public void setSuKienId(int suKienId) { this.suKienId = suKienId; }

    public int getVdv1Id() { return vdv1Id; }
    public void setVdv1Id(int vdv1Id) { this.vdv1Id = vdv1Id; }

    public int getVdv2Id() { return vdv2Id; }
    public void setVdv2Id(int vdv2Id) { this.vdv2Id = vdv2Id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Cặp đôi #" + capdoiId;
    }
}
