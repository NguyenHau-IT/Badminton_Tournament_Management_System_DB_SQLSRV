package com.example.btms.service.draw;

import com.example.btms.model.draw.BocThamDoi;
import com.example.btms.repository.draw.BocThamDoiRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class BocThamDoiService {

    private final Connection conn; // dùng khi cần transaction (batch/đảo thứ tự)
    private final BocThamDoiRepository repo;

    public BocThamDoiService(Connection conn, BocThamDoiRepository repo) {
        this.conn = Objects.requireNonNull(conn);
        this.repo = Objects.requireNonNull(repo);
    }

    /* ===== CRUD cơ bản ===== */

    public void create(int idGiai, int idNoiDung, Integer idClb,
            String tenTeam, int thuTu, Integer soDo) {
        validateRow(tenTeam, thuTu);
        int clbVal = (idClb != null) ? idClb : 0;
        int soDoVal = (soDo != null) ? soDo : 1;
        repo.add(new BocThamDoi(idGiai, idNoiDung, clbVal, tenTeam.trim(), thuTu, soDoVal));
    }

    public BocThamDoi getOne(int idGiai, int idNoiDung, int thuTu) {
        BocThamDoi r = repo.findOne(idGiai, idNoiDung, thuTu);
        if (r == null)
            throw new NoSuchElementException("Không tìm thấy dòng bốc thăm");
        return r;
    }

    public List<BocThamDoi> list(int idGiai, int idNoiDung) {
        return repo.listBy(idGiai, idNoiDung);
    }

    public void update(int idGiai, int idNoiDung, int thuTu,
            Integer idClb, String tenTeam, Integer soDo) {
        validateRow(tenTeam, thuTu);
        getOne(idGiai, idNoiDung, thuTu); // đảm bảo tồn tại
        int clbVal = (idClb != null) ? idClb : 0;
        int soDoVal = (soDo != null) ? soDo : 1;
        repo.update(new BocThamDoi(idGiai, idNoiDung, clbVal, tenTeam.trim(), thuTu, soDoVal));
    }

    public void delete(int idGiai, int idNoiDung, int thuTu) {
        getOne(idGiai, idNoiDung, thuTu);
        repo.delete(idGiai, idNoiDung, thuTu);
    }

    /* ===== Tiện ích thêm: set lại thứ tự / bốc thăm ngẫu nhiên ===== */

    /** Đổi thứ tự một mục (khóa thay đổi). */
    public void changeOrder(int idGiai, int idNoiDung, int oldThuTu, int newThuTu) {
        if (newThuTu < 0)
            throw new IllegalArgumentException("THU_TU phải >= 0");
        repo.updateOrder(idGiai, idNoiDung, oldThuTu, newThuTu);
    }

    /**
     * Ghi danh sách đội theo thứ tự bốc thăm đã cho (transaction).
     * Xoá hết cũ rồi ghi mới để đảm bảo sạch dữ liệu.
     */
    public void resetWithOrder(int idGiai, int idNoiDung, List<BocThamDoi> rows) {
        Objects.requireNonNull(rows, "rows null");
        boolean oldAuto = true;
        try {
            oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // xóa toàn bộ
            for (BocThamDoi r : repo.listBy(idGiai, idNoiDung)) {
                repo.delete(idGiai, idNoiDung, r.getThuTu());
            }
            // chèn lại
            int i = 0; // bắt đầu từ 0 theo yêu cầu mới
            for (BocThamDoi r : rows) {
                validateRow(r.getTenTeam(), i);
                int clbVal = (r.getIdClb() != null) ? r.getIdClb() : 0;
                int soDoVal = (r.getSoDo() != null) ? r.getSoDo() : 1;
                repo.add(new BocThamDoi(
                        idGiai, idNoiDung, clbVal,
                        r.getTenTeam().trim(), i, soDoVal));
                i++;
            }

            conn.commit();
        } catch (RuntimeException | SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            throw new RuntimeException("Cập nhật lại danh sách bốc thăm thất bại", e);
        } finally {
            try {
                conn.setAutoCommit(oldAuto);
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * Bốc thăm ngẫu nhiên từ danh sách đội (tên + ID_CLB tùy chọn), auto THU_TU từ
     * 1..n.
     */
    public void shuffleAndInsert(int idGiai, int idNoiDung, List<BocThamDoi> teams) {
        Collections.shuffle(teams, new Random());
        resetWithOrder(idGiai, idNoiDung, teams);
    }

    /* ===== helpers ===== */
    private void validateRow(String tenTeam, int thuTu) {
        if (tenTeam == null || tenTeam.trim().isEmpty())
            throw new IllegalArgumentException("TEN_TEAM không được rỗng");
        if (thuTu < 0)
            throw new IllegalArgumentException("THU_TU phải >= 0");
    }
}
