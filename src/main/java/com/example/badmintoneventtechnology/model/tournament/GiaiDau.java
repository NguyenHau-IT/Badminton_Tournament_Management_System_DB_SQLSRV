package com.example.badmintoneventtechnology.model.tournament;

import java.time.LocalDateTime;

/**
 * Entity class cho bảng GIAI_DAU
 * Tương ứng với schema database đã được cung cấp
 */
public class GiaiDau {
    private Long id;
    private String tenGiai;
    private LocalDateTime ngayBd;
    private LocalDateTime ngayKt;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Long idUser;

    // Constructors
    public GiaiDau() {
    }

    public GiaiDau(String tenGiai, LocalDateTime ngayBd, LocalDateTime ngayKt, Long idUser) {
        this.tenGiai = tenGiai;
        this.ngayBd = ngayBd;
        this.ngayKt = ngayKt;
        this.idUser = idUser;
        this.ngayTao = LocalDateTime.now();
        this.ngayCapNhat = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenGiai() {
        return tenGiai;
    }

    public void setTenGiai(String tenGiai) {
        this.tenGiai = tenGiai;
    }

    public LocalDateTime getNgayBd() {
        return ngayBd;
    }

    public void setNgayBd(LocalDateTime ngayBd) {
        this.ngayBd = ngayBd;
    }

    public LocalDateTime getNgayKt() {
        return ngayKt;
    }

    public void setNgayKt(LocalDateTime ngayKt) {
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

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    // Utility methods
    public void updateTimestamp() {
        this.ngayCapNhat = LocalDateTime.now();
    }

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return ngayBd != null && ngayKt != null &&
                now.isAfter(ngayBd) && now.isBefore(ngayKt);
    }

    public boolean isUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        return ngayBd != null && now.isBefore(ngayBd);
    }

    public boolean isFinished() {
        LocalDateTime now = LocalDateTime.now();
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
