package com.example.badmintoneventtechnology.model.tournament;

public class TySo {
    private int tysoId;
    private int tranId;
    private int soGame;
    private int diemTren;
    private int diemDuoi;

    public TySo() {}

    public TySo(int tranId, int soGame, int diemTren, int diemDuoi) {
        this.tranId = tranId;
        this.soGame = soGame;
        this.diemTren = diemTren;
        this.diemDuoi = diemDuoi;
    }

    // Getters and Setters
    public int getTysoId() { return tysoId; }
    public void setTysoId(int tysoId) { this.tysoId = tysoId; }

    public int getTranId() { return tranId; }
    public void setTranId(int tranId) { this.tranId = tranId; }

    public int getSoGame() { return soGame; }
    public void setSoGame(int soGame) { this.soGame = soGame; }

    public int getDiemTren() { return diemTren; }
    public void setDiemTren(int diemTren) { this.diemTren = diemTren; }

    public int getDiemDuoi() { return diemDuoi; }
    public void setDiemDuoi(int diemDuoi) { this.diemDuoi = diemDuoi; }

    @Override
    public String toString() {
        return "Game " + soGame + ": " + diemTren + "-" + diemDuoi;
    }
}
