package com.example.btms.model.bracket;

import java.time.LocalDateTime;
import java.util.Objects;

public class SoDoCaNhan {
    private Integer idGiai;
    private Integer idNoiDung;
    private Integer idVdv;
    private Integer toaDoX;
    private Integer toaDoY;
    private Integer viTri;
    private Integer soDo;
    private LocalDateTime thoiGian;

    public SoDoCaNhan() {
    }

    public SoDoCaNhan(Integer idGiai, Integer idNoiDung, Integer idVdv,
            Integer toaDoX, Integer toaDoY, Integer viTri,
            Integer soDo, LocalDateTime thoiGian) {
        this.idGiai = Objects.requireNonNull(idGiai, "ID_GIAI null");
        this.idNoiDung = Objects.requireNonNull(idNoiDung, "ID_NOI_DUNG null");
        this.idVdv = Objects.requireNonNull(idVdv, "ID_VDV null");
        this.toaDoX = Objects.requireNonNull(toaDoX, "TOA_DO_X null");
        this.toaDoY = Objects.requireNonNull(toaDoY, "TOA_DO_Y null");
        this.viTri = Objects.requireNonNull(viTri, "VI_TRI null");
        this.soDo = Objects.requireNonNull(soDo, "SO_DO null");
        this.thoiGian = Objects.requireNonNull(thoiGian, "THOI_GIAN null");
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

    public Integer getToaDoX() {
        return toaDoX;
    }

    public void setToaDoX(Integer toaDoX) {
        this.toaDoX = toaDoX;
    }

    public Integer getToaDoY() {
        return toaDoY;
    }

    public void setToaDoY(Integer toaDoY) {
        this.toaDoY = toaDoY;
    }

    public Integer getViTri() {
        return viTri;
    }

    public void setViTri(Integer viTri) {
        this.viTri = viTri;
    }

    public Integer getSoDo() {
        return soDo;
    }

    public void setSoDo(Integer soDo) {
        this.soDo = soDo;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }
}