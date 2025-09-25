package com.example.btms.model.player;

import java.util.Objects;

/**
 * Đăng ký cá nhân vào một nội dung của một giải.
 * Giả định bảng: DANG_KI_CA_NHAN(ID IDENTITY PK, ID_GIAI, ID_NOI_DUNG, ID_VDV)
 */
public class DangKyCaNhan {
    private Integer id; // PK
    private Integer idGiai; // FK -> GIAI_DAU.ID (giả định)
    private Integer idNoiDung; // FK -> NOI_DUNG.ID
    private Integer idVdv; // FK -> VAN_DONG_VIEN.ID

    public DangKyCaNhan() {}

    public DangKyCaNhan(Integer id, Integer idGiai, Integer idNoiDung, Integer idVdv) {
        this.id = id;
        this.idGiai = idGiai;
        this.idNoiDung = idNoiDung;
        this.idVdv = idVdv;
    }

    public DangKyCaNhan(Integer idGiai, Integer idNoiDung, Integer idVdv) {
        this(null, idGiai, idNoiDung, idVdv);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getIdGiai() { return idGiai; }
    public void setIdGiai(Integer idGiai) { this.idGiai = idGiai; }
    public Integer getIdNoiDung() { return idNoiDung; }
    public void setIdNoiDung(Integer idNoiDung) { this.idNoiDung = idNoiDung; }
    public Integer getIdVdv() { return idVdv; }
    public void setIdVdv(Integer idVdv) { this.idVdv = idVdv; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DangKyCaNhan)) return false;
        DangKyCaNhan that = (DangKyCaNhan) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "DangKyCaNhan{" +
                "id=" + id +
                ", idGiai=" + idGiai +
                ", idNoiDung=" + idNoiDung +
                ", idVdv=" + idVdv +
                '}';
    }
}
