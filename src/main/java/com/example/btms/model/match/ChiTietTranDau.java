package com.example.btms.model.match;

import java.time.LocalDateTime;
import java.util.Objects;

public class ChiTietTranDau {
    private String id; // char(36)
    private Integer theThuc; // tinyint
    private Integer idVdvThang; // int
    private LocalDateTime batDau; // datetime
    private LocalDateTime ketThuc; // datetime
    private Integer san; // int

    public ChiTietTranDau() {
    }

    public ChiTietTranDau(String id, Integer theThuc, Integer idVdvThang,
            LocalDateTime batDau, LocalDateTime ketThuc, Integer san) {
        this.id = Objects.requireNonNull(id);
        this.theThuc = Objects.requireNonNull(theThuc);
        this.idVdvThang = Objects.requireNonNull(idVdvThang);
        this.batDau = Objects.requireNonNull(batDau);
        this.ketThuc = Objects.requireNonNull(ketThuc);
        this.san = Objects.requireNonNull(san);
    }

    // getters/setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getTheThuc() {
        return theThuc;
    }

    public void setTheThuc(Integer theThuc) {
        this.theThuc = theThuc;
    }

    public Integer getIdVdvThang() {
        return idVdvThang;
    }

    public void setIdVdvThang(Integer idVdvThang) {
        this.idVdvThang = idVdvThang;
    }

    public LocalDateTime getBatDau() {
        return batDau;
    }

    public void setBatDau(LocalDateTime batDau) {
        this.batDau = batDau;
    }

    public LocalDateTime getKetThuc() {
        return ketThuc;
    }

    public void setKetThuc(LocalDateTime ketThuc) {
        this.ketThuc = ketThuc;
    }

    public Integer getSan() {
        return san;
    }

    public void setSan(Integer san) {
        this.san = san;
    }
}
