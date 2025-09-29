package com.example.btms.model.result;

import java.util.Objects;

public class KetQuaCaNhan {
    private Integer idGiai;
    private Integer idNoiDung;
    private Integer idVdv;
    private Integer thuHang;

    public KetQuaCaNhan() {
    }

    public KetQuaCaNhan(Integer idGiai, Integer idNoiDung, Integer idVdv, Integer thuHang) {
        this.idGiai = Objects.requireNonNull(idGiai, "ID_GIAI null");
        this.idNoiDung = Objects.requireNonNull(idNoiDung, "ID_NOI_DUNG null");
        this.idVdv = Objects.requireNonNull(idVdv, "ID_VDV null");
        this.thuHang = Objects.requireNonNull(thuHang, "THU_HANG null");
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

    public Integer getIdVdv() {
        return idVdv;
    }

    public void setIdVdv(Integer idVdv) {
        this.idVdv = idVdv;
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
        if (!(o instanceof KetQuaCaNhan))
            return false;
        KetQuaCaNhan that = (KetQuaCaNhan) o;
        return idGiai.equals(that.idGiai) &&
                idNoiDung.equals(that.idNoiDung) &&
                thuHang.equals(that.thuHang);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idGiai, idNoiDung, thuHang);
    }
}
