package com.example.btms.model.player;

import java.time.LocalDateTime;
import java.util.Objects;

public class DangKiCaNhan {
    private Integer idGiai;
    private Integer idNoiDung;
    private Integer idVdv;
    private LocalDateTime thoiGianTao;

    public DangKiCaNhan() {
    }

    public DangKiCaNhan(Integer idGiai, Integer idNoiDung, Integer idVdv, LocalDateTime thoiGianTao) {
        this.idGiai = idGiai;
        this.idNoiDung = idNoiDung;
        this.idVdv = idVdv;
        this.thoiGianTao = thoiGianTao;
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

    public LocalDateTime getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(LocalDateTime thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DangKiCaNhan))
            return false;
        DangKiCaNhan that = (DangKiCaNhan) o;
        return Objects.equals(idGiai, that.idGiai) &&
                Objects.equals(idNoiDung, that.idNoiDung) &&
                Objects.equals(idVdv, that.idVdv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idGiai, idNoiDung, idVdv);
    }
}
