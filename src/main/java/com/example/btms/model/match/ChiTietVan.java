package com.example.btms.model.match;

import java.util.Objects;

public class ChiTietVan {
    private String idTranDau; // char(36)
    private Integer setNo; // tinyint
    private Integer tongDiem1; // tinyint
    private Integer tongDiem2; // tinyint
    private String dauThoiGian; // nvarchar(MAX)

    public ChiTietVan() {
    }

    public ChiTietVan(String idTranDau, Integer setNo, Integer tongDiem1,
            Integer tongDiem2, String dauThoiGian) {
        this.idTranDau = Objects.requireNonNull(idTranDau);
        this.setNo = Objects.requireNonNull(setNo);
        this.tongDiem1 = Objects.requireNonNull(tongDiem1);
        this.tongDiem2 = Objects.requireNonNull(tongDiem2);
        this.dauThoiGian = Objects.requireNonNull(dauThoiGian);
    }

    // getters/setters
    public String getIdTranDau() {
        return idTranDau;
    }

    public void setIdTranDau(String idTranDau) {
        this.idTranDau = idTranDau;
    }

    public Integer getSetNo() {
        return setNo;
    }

    public void setSetNo(Integer setNo) {
        this.setNo = setNo;
    }

    public Integer getTongDiem1() {
        return tongDiem1;
    }

    public void setTongDiem1(Integer tongDiem1) {
        this.tongDiem1 = tongDiem1;
    }

    public Integer getTongDiem2() {
        return tongDiem2;
    }

    public void setTongDiem2(Integer tongDiem2) {
        this.tongDiem2 = tongDiem2;
    }

    public String getDauThoiGian() {
        return dauThoiGian;
    }

    public void setDauThoiGian(String dauThoiGian) {
        this.dauThoiGian = dauThoiGian;
    }
}
