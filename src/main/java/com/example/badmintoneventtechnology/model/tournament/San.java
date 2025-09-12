package com.example.badmintoneventtechnology.model.tournament;

public class San {
    private int sanId;
    private int giaiId;
    private String ten;

    public San() {}

    public San(int giaiId, String ten) {
        this.giaiId = giaiId;
        this.ten = ten;
    }

    // Getters and Setters
    public int getSanId() { return sanId; }
    public void setSanId(int sanId) { this.sanId = sanId; }

    public int getGiaiId() { return giaiId; }
    public void setGiaiId(int giaiId) { this.giaiId = giaiId; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    @Override
    public String toString() {
        return ten;
    }
}
