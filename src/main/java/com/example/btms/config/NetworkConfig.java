package com.example.btms.config;

import java.util.Objects;

/** Chỉ lưu tên interface người dùng chọn (không ràng buộc IP/port). */
public class NetworkConfig {
    private final String ifName;

    public NetworkConfig(String ifName) {
        this.ifName = ifName;
    }

    public String ifName() {
        return ifName;
    }

    @Override
    public String toString() {
        return "NetworkConfig{ifName='" + ifName + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof NetworkConfig that))
            return false;
        return Objects.equals(ifName, that.ifName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ifName);
    }
}
