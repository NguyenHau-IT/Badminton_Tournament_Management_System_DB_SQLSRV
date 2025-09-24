package com.example.btms.model.team;

import java.util.Objects;

public class ChiTietDoi {
    private Integer idTeam; // FK -> DANG_KI_DOI.ID_TEAM
    private Integer idVdv; // FK -> VAN_DONG_VIEN.ID

    public ChiTietDoi() {
    }

    public ChiTietDoi(Integer idTeam, Integer idVdv) {
        this.idTeam = idTeam;
        this.idVdv = idVdv;
    }

    public Integer getIdTeam() {
        return idTeam;
    }

    public void setIdTeam(Integer idTeam) {
        this.idTeam = idTeam;
    }

    public Integer getIdVdv() {
        return idVdv;
    }

    public void setIdVdv(Integer idVdv) {
        this.idVdv = idVdv;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChiTietDoi))
            return false;
        ChiTietDoi that = (ChiTietDoi) o;
        return Objects.equals(idTeam, that.idTeam) && Objects.equals(idVdv, that.idVdv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTeam, idVdv);
    }
}
