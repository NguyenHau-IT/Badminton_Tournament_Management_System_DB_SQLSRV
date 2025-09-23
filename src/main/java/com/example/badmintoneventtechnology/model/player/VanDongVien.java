package com.example.badmintoneventtechnology.model.player;

import java.time.LocalDate;
import java.util.Objects;

public class VanDongVien {
    private Integer id; // auto-increment
    private String hoTen;
    private LocalDate ngaySinh;
    private Integer idClb; // FK đến CAU_LAC_BO
    private String gioiTinh; // "M" = Nam, "F" = Nữ, "O" = Khác

    public VanDongVien() {
    }

    public VanDongVien(Integer id, String hoTen, LocalDate ngaySinh, Integer idClb, String gioiTinh) {
        this.id = id;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.idClb = idClb;
        this.gioiTinh = gioiTinh;
    }

    public VanDongVien(String hoTen, LocalDate ngaySinh, Integer idClb, String gioiTinh) {
        this(null, hoTen, ngaySinh, idClb, gioiTinh);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public Integer getIdClb() {
        return idClb;
    }

    public void setIdClb(Integer idClb) {
        this.idClb = idClb;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof VanDongVien))
            return false;
        VanDongVien that = (VanDongVien) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "VanDongVien{id=" + id +
                ", hoTen='" + hoTen + '\'' +
                ", ngaySinh=" + ngaySinh +
                ", idClb=" + idClb +
                ", gioiTinh='" + gioiTinh + '\'' +
                '}';
    }
}
