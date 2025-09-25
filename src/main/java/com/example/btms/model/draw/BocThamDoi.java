package com.example.btms.model.draw;

import java.util.Objects;

public class BocThamDoi {
    private Integer idGiai;
    private Integer idNoiDung;
    private Integer idClb; // có thể null
    private String tenTeam; // không null / rỗng
    private Integer thuTu; // khóa thành phần
    private Integer soDo; // có thể null

    public BocThamDoi() {
    }

    public BocThamDoi(Integer idGiai, Integer idNoiDung, Integer idClb,
            String tenTeam, Integer thuTu, Integer soDo) {
        this.idGiai = idGiai;
        this.idNoiDung = idNoiDung;
        this.idClb = idClb;
        this.tenTeam = tenTeam;
        this.thuTu = thuTu;
        this.soDo = soDo;
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

    public Integer getThuTu() {
        return thuTu;
    }

    public void setThuTu(Integer thuTu) {
        this.thuTu = thuTu;
    }

    public Integer getSoDo() {
        return soDo;
    }

    public void setSoDo(Integer soDo) {
        this.soDo = soDo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BocThamDoi))
            return false;
        BocThamDoi that = (BocThamDoi) o;
        return Objects.equals(idGiai, that.idGiai)
                && Objects.equals(idNoiDung, that.idNoiDung)
                && Objects.equals(thuTu, that.thuTu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idGiai, idNoiDung, thuTu);
    }
}
