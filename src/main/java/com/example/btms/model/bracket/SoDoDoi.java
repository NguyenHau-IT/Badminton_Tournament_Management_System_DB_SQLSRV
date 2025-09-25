package com.example.btms.model.bracket;

import java.time.LocalDateTime;
import java.util.Objects;

public class SoDoDoi {
    private Integer idGiai;
    private Integer idNoiDung;
    private Integer idClb;
    private String tenTeam;
    private Integer toaDoX;
    private Integer toaDoY;
    private Integer viTri; // khóa cùng với ID_GIAI, ID_NOI_DUNG
    private Integer soDo; // vòng/nhánh
    private LocalDateTime thoiGian;

    public SoDoDoi() {
    }

    public SoDoDoi(Integer idGiai, Integer idNoiDung, Integer idClb,
            String tenTeam, Integer toaDoX, Integer toaDoY,
            Integer viTri, Integer soDo, LocalDateTime thoiGian) {
        this.idGiai = idGiai;
        this.idNoiDung = idNoiDung;
        this.idClb = idClb;
        this.tenTeam = tenTeam;
        this.toaDoX = toaDoX;
        this.toaDoY = toaDoY;
        this.viTri = viTri;
        this.soDo = soDo;
        this.thoiGian = thoiGian;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SoDoDoi))
            return false;
        SoDoDoi that = (SoDoDoi) o;
        return Objects.equals(idGiai, that.idGiai)
                && Objects.equals(idNoiDung, that.idNoiDung)
                && Objects.equals(viTri, that.viTri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idGiai, idNoiDung, viTri);
    }
}
