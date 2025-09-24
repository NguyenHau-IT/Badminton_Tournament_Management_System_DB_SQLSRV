package com.example.btms.model.team;

import java.util.Objects;

public class DangKiDoi {
    private Integer idTeam; // auto-increment
    private Integer idGiai;
    private Integer idNoiDung;
    private Integer idCauLacBo; // có thể null (đội tự do)
    private String tenTeam;

    public DangKiDoi() {
    }

    public DangKiDoi(Integer idTeam, Integer idGiai, Integer idNoiDung,
            Integer idCauLacBo, String tenTeam) {
        this.idTeam = idTeam;
        this.idGiai = idGiai;
        this.idNoiDung = idNoiDung;
        this.idCauLacBo = idCauLacBo;
        this.tenTeam = tenTeam;
    }

    public DangKiDoi(Integer idGiai, Integer idNoiDung, Integer idCauLacBo, String tenTeam) {
        this(null, idGiai, idNoiDung, idCauLacBo, tenTeam);
    }

    public Integer getIdTeam() {
        return idTeam;
    }

    public void setIdTeam(Integer idTeam) {
        this.idTeam = idTeam;
    }

    public Integer getIdGiai() {
        return idGiai;
    }

    public void setIdGiai(Integer idGiai) {
        this.idGiai = idGiai;
    }

    public Integer getIdNoiDung() {
        return idNoiDung;
    }

    public void setIdNoiDung(Integer idNoiDung) {
        this.idNoiDung = idNoiDung;
    }

    public Integer getIdCauLacBo() {
        return idCauLacBo;
    }

    public void setIdCauLacBo(Integer idCauLacBo) {
        this.idCauLacBo = idCauLacBo;
    }

    public String getTenTeam() {
        return tenTeam;
    }

    public void setTenTeam(String tenTeam) {
        this.tenTeam = tenTeam;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DangKiDoi))
            return false;
        DangKiDoi that = (DangKiDoi) o;
        return Objects.equals(idTeam, that.idTeam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTeam);
    }

    @Override
    public String toString() {
        // Dùng tên đội nếu có, fallback hiển thị #ID để tránh null
        return (tenTeam != null && !tenTeam.isBlank()) ? tenTeam : (idTeam != null ? ("#" + idTeam) : "(đội)");
    }
}
