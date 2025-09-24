package com.example.btms.model.tournament;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity class cho bảng GIAI_DAU
 * Tương ứng với schema database đã được cung cấp
 */
public class GiaiDau {
    private Integer id;
    private String tenGiai;
    private LocalDate ngayBd;
    private LocalDate ngayKt;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Integer idUser;

    // Constructors
    public GiaiDau() {
    }

    public GiaiDau(String tenGiai, LocalDate ngayBd, LocalDate ngayKt, Integer idUser) {
        this.tenGiai = tenGiai;
        this.ngayBd = ngayBd;
        this.ngayKt = ngayKt;
        this.idUser = idUser;
        this.ngayTao = LocalDateTime.now();
        this.ngayCapNhat = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenGiai() {
        return tenGiai;
    }

    public void setTenGiai(String tenGiai) {
        this.tenGiai = tenGiai;
    }

    public LocalDate getNgayBd() {
        return ngayBd;
    }

    public void setNgayBd(LocalDate ngayBd) {
        this.ngayBd = ngayBd;
    }

    public LocalDate getNgayKt() {
        return ngayKt;
    }

    public void setNgayKt(LocalDate ngayKt) {
        this.ngayKt = ngayKt;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    // Utility methods
    public void updatestamp() {
        this.ngayCapNhat = LocalDateTime.now();
    }

    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return ngayBd != null && ngayKt != null &&
                now.isAfter(ngayBd) && now.isBefore(ngayKt);
    }

    public boolean isUpcoming() {
        LocalDate now = LocalDate.now();
        return ngayBd != null && now.isBefore(ngayBd);
    }

    public boolean isFinished() {
        LocalDate now = LocalDate.now();
        return ngayKt != null && now.isAfter(ngayKt);
    }

    @Override
    public String toString() {
        return "GiaiDau{" +
                "id=" + id +
                ", tenGiai='" + tenGiai + '\'' +
                ", ngayBd=" + ngayBd +
                ", ngayKt=" + ngayKt +
                ", ngayTao=" + ngayTao +
                ", ngayCapNhat=" + ngayCapNhat +
                ", idUser=" + idUser +
                '}';
    }
}
