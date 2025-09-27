package com.example.btms.model.result;

import java.util.Objects;

public class KetQuaDoi {
    private Integer idGiai; // NOT NULL
    private Integer idNoiDung; // NOT NULL
    private Integer idClb; // NOT NULL
    private String tenTeam; // NOT NULL
    private Integer thuHang; // NOT NULL

    public KetQuaDoi() {
    }

    public KetQuaDoi(Integer idGiai, Integer idNoiDung, Integer idClb, String tenTeam, Integer thuHang) {
        this.idGiai = Objects.requireNonNull(idGiai, "ID_GIAI không được null");
        this.idNoiDung = Objects.requireNonNull(idNoiDung, "ID_NOI_DUNG không được null");
        this.idClb = Objects.requireNonNull(idClb, "ID_CLB không được null");
        this.tenTeam = Objects.requireNonNull(tenTeam, "TEN_TEAM không được null");
        this.thuHang = Objects.requireNonNull(thuHang, "THU_HANG không được null");
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

    public Integer getIdClb() {
        return idClb;
    }

    public void setIdClb(Integer idClb) {
        this.idClb = idClb;
    }

    public String getTenTeam() {
        return tenTeam;
    }

    public void setTenTeam(String tenTeam) {
        this.tenTeam = tenTeam;
    }

    public Integer getThuHang() {
        return thuHang;
    }

    public void setThuHang(Integer thuHang) {
        this.thuHang = thuHang;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof KetQuaDoi))
            return false;
        KetQuaDoi that = (KetQuaDoi) o;
        return Objects.equals(idGiai, that.idGiai) &&
                Objects.equals(idNoiDung, that.idNoiDung) &&
                Objects.equals(thuHang, that.thuHang);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idGiai, idNoiDung, thuHang);
    }
}
