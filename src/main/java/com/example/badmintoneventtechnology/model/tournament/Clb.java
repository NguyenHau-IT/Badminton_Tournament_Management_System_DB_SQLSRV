package com.example.badmintoneventtechnology.model.tournament;

public class Clb {
    private int clbId;
    private String ten;
    private String thanhPho;
    private String quocGia;

    public Clb() {}

    public Clb(String ten, String thanhPho, String quocGia) {
        this.ten = ten;
        this.thanhPho = thanhPho;
        this.quocGia = quocGia;
    }

    // Getters and Setters
    public int getClbId() { return clbId; }
    public void setClbId(int clbId) { this.clbId = clbId; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getThanhPho() { return thanhPho; }
    public void setThanhPho(String thanhPho) { this.thanhPho = thanhPho; }

    public String getQuocGia() { return quocGia; }
    public void setQuocGia(String quocGia) { this.quocGia = quocGia; }

    @Override
    public String toString() {
        return ten + (thanhPho != null ? " - " + thanhPho : "");
    }
}
