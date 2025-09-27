package com.example.btms.model.draw;

import java.util.Objects;

public class BocThamCaNhan {
    private Integer idGiai;
    private Integer idNoiDung;
    private Integer idVdv;
    private Integer thuTu;
    private Integer soDo;

    public BocThamCaNhan() {}

    public BocThamCaNhan(Integer idGiai, Integer idNoiDung, Integer idVdv, Integer thuTu, Integer soDo) {
        this.idGiai = Objects.requireNonNull(idGiai, "ID_GIAI null");
        this.idNoiDung = Objects.requireNonNull(idNoiDung, "ID_NOI_DUNG null");
        this.idVdv = Objects.requireNonNull(idVdv, "ID_VDV null");
        this.thuTu = Objects.requireNonNull(thuTu, "THU_TU null");
        this.soDo = Objects.requireNonNull(soDo, "SO_DO null");
    }

    public Integer getIdGiai() { return idGiai; }
    public void setIdGiai(Integer idGiai) { this.idGiai = idGiai; }
    public Integer getIdNoiDung() { return idNoiDung; }
    public void setIdNoiDung(Integer idNoiDung) { this.idNoiDung = idNoiDung; }
    public Integer getIdVdv() { return idVdv; }
    public void setIdVdv(Integer idVdv) { this.idVdv = idVdv; }
    public Integer getThuTu() { return thuTu; }
    public void setThuTu(Integer thuTu) { this.thuTu = thuTu; }
    public Integer getSoDo() { return soDo; }
    public void setSoDo(Integer soDo) { this.soDo = soDo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BocThamCaNhan)) return false;
        BocThamCaNhan that = (BocThamCaNhan) o;
        return Objects.equals(idGiai, that.idGiai) &&
               Objects.equals(idNoiDung, that.idNoiDung) &&
               Objects.equals(idVdv, that.idVdv);
    }

    @Override
    public int hashCode() { return Objects.hash(idGiai, idNoiDung, idVdv); }
}