package com.example.badmintoneventtechnology.model.club;

import java.util.Objects;

public class CauLacBo {
    private Integer id;
    private String tenClb;
    private String tenNgan; // thêm cột mới

    public CauLacBo() {
    }

    public CauLacBo(Integer id, String tenClb, String tenNgan) {
        this.id = id;
        this.tenClb = tenClb;
        this.tenNgan = tenNgan;
    }

    public CauLacBo(String tenClb, String tenNgan) {
        this.tenClb = tenClb;
        this.tenNgan = tenNgan;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenClb() {
        return tenClb;
    }

    public void setTenClb(String tenClb) {
        this.tenClb = tenClb;
    }

    public String getTenNgan() {
        return tenNgan;
    }

    public void setTenNgan(String tenNgan) {
        this.tenNgan = tenNgan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CauLacBo))
            return false;
        CauLacBo c = (CauLacBo) o;
        return Objects.equals(id, c.id) &&
                Objects.equals(tenClb, c.tenClb) &&
                Objects.equals(tenNgan, c.tenNgan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tenClb, tenNgan);
    }

    @Override
    public String toString() {
        return "CauLacBo{id=" + id + ", tenClb='" + tenClb + "', tenNgan='" + tenNgan + "'}";
    }
}
