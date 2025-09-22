package com.example.badmintoneventtechnology.model.tournament;

/**
 * Model cho thông tin giải đấu từ bảng VERANSTALTUNG
 */
public class Tournament {
    private final int vernr; // ID giải đấu
    private final String bezeichnung; // Tên giải đấu

    public Tournament(int vernr, String bezeichnung) {
        this.vernr = vernr;
        this.bezeichnung = bezeichnung;
    }

    public int getVernr() {
        return vernr;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    @Override
    public String toString() {
        return bezeichnung;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Tournament that = (Tournament) obj;
        return vernr == that.vernr;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(vernr);
    }
}
