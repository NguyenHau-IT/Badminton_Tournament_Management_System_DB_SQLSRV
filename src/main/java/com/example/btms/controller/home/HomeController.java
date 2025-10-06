package com.example.btms.controller.home;

import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.btms.config.ConnectionConfig;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.player.VanDongVien;
import com.example.btms.model.result.KetQuaCaNhan;
import com.example.btms.model.result.KetQuaDoi;
import com.example.btms.model.tournament.GiaiDau;
import com.example.btms.model.db.SQLSRVConnectionManager;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.repository.player.DangKiCaNhanRepository;
import com.example.btms.repository.player.VanDongVienRepository;
import com.example.btms.repository.result.KetQuaCaNhanRepository;
import com.example.btms.repository.result.KetQuaDoiRepository;
import com.example.btms.repository.team.DangKiDoiRepository;
import com.example.btms.repository.tuornament.GiaiDauRepository;

@Controller
public class HomeController {

    @Autowired
    private ConnectionConfig dbCfg;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Trang chủ: liệt kê các giải đấu đã tổ chức và sắp xếp theo ngày tạo mới nhất */
    @GetMapping({"/", "/home"})
    public String home(
            Model model,
            @org.springframework.web.bind.annotation.RequestParam(name = "q", required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(name = "from", required = false) java.time.LocalDate from,
            @org.springframework.web.bind.annotation.RequestParam(name = "to", required = false) java.time.LocalDate to,
            @org.springframework.web.bind.annotation.RequestParam(name = "status", required = false, defaultValue = "ALL") String status,
            @org.springframework.web.bind.annotation.RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(name = "size", required = false, defaultValue = "9") int size) {
        try (SQLSRVConnectionManager mgr = new SQLSRVConnectionManager()) {
            mgr.setConfig(dbCfg);
            Connection conn = mgr.connect();

            var giaiRepo = new GiaiDauRepository(conn);
            var noiDungRepo = new NoiDungRepository(conn);
            var dkCaNhanRepo = new DangKiCaNhanRepository(conn);
            var dkDoiRepo = new DangKiDoiRepository(conn);

            List<GiaiDau> list = giaiRepo.findAll();

            // Lọc theo tên và khoảng ngày nếu có
            if (q != null && !q.isBlank()) {
                String query = q.trim().toLowerCase();
                list.removeIf(gd -> gd.getTenGiai() == null || !gd.getTenGiai().toLowerCase().contains(query));
            }
            // Chuẩn hóa khoảng ngày (không gán lại tham số để dùng trong lambda)
            java.time.LocalDate f = from;
            java.time.LocalDate t = to;
            if (f != null && t != null && f.isAfter(t)) {
                java.time.LocalDate tmp = f; f = t; t = tmp;
            }
            final java.time.LocalDate ff = f;
            final java.time.LocalDate tt = t;
            if (ff != null) {
                list.removeIf(gd -> gd.getNgayBd() == null || gd.getNgayBd().isBefore(ff));
            }
            if (tt != null) {
                list.removeIf(gd -> gd.getNgayKt() == null || gd.getNgayKt().isAfter(tt));
            }

            // Lọc theo trạng thái: UPCOMING (chưa diễn ra), ONGOING (đang diễn ra), FINISHED (đã kết thúc), ALL.
            java.time.LocalDate today = java.time.LocalDate.now();
            String st = status == null ? "UPCOMING" : status.trim().toUpperCase();
            switch (st) {
                case "UPCOMING":
                    list.removeIf(gd -> gd.getNgayBd() == null || !today.isBefore(gd.getNgayBd()));
                    break;
                case "ONGOING":
                    list.removeIf(gd -> gd.getNgayBd() == null || gd.getNgayKt() == null || !( (today.isEqual(gd.getNgayBd()) || today.isAfter(gd.getNgayBd())) && (today.isBefore(gd.getNgayKt()) || today.isEqual(gd.getNgayKt())) ));
                    break;
                case "FINISHED":
                    list.removeIf(gd -> gd.getNgayKt() == null || !today.isAfter(gd.getNgayKt()));
                    break;
                default:
                    // ALL: không lọc thêm
                    st = "UPCOMING";
            }

            // Phân trang thủ công
            int total = list.size();
            if (size < 1) size = 9;
            if (page < 0) page = 0;
            int totalPages = (int) Math.ceil(total / (double) size);
            if (totalPages > 0 && page >= totalPages) page = Math.max(0, totalPages - 1);
            int fromIdx = page * size;
            int toIdx = Math.min(fromIdx + size, total);
            List<GiaiDau> pageList = fromIdx < toIdx ? list.subList(fromIdx, toIdx) : java.util.List.of();
            List<Map<String, Object>> summaries = new ArrayList<>();
            for (GiaiDau gd : pageList) {
                Map<String, Object> m = new HashMap<>();
                m.put("giai", gd);

                List<NoiDung> nds = noiDungRepo.findByTournament(gd.getId());
                int totalContents = nds.size();
                int totalSinglesRegs = 0;
                int totalTeamRegs = 0;
                for (NoiDung nd : nds) {
                    boolean isTeam = nd.getTeam() != null && nd.getTeam();
                    if (isTeam) {
                        totalTeamRegs += dkDoiRepo.findAllBy(gd.getId(), nd.getId()).size();
                    } else {
                        totalSinglesRegs += dkCaNhanRepo.list(gd.getId(), nd.getId()).size();
                    }
                }
                m.put("totalContents", totalContents);
                m.put("totalSinglesRegs", totalSinglesRegs);
                m.put("totalTeamRegs", totalTeamRegs);
                summaries.add(m);
            }

            model.addAttribute("summaries", summaries);
            model.addAttribute("dateFmt", DATE_FMT);
            // Giữ lại tham số tìm kiếm để populate UI
            model.addAttribute("q", q);
            model.addAttribute("from", from);
            model.addAttribute("to", to);
            model.addAttribute("status", st);
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            model.addAttribute("total", total);
            model.addAttribute("totalPages", totalPages);
            return "home/home";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải danh sách giải đấu: " + e.getMessage());
            return "exception/error";
        }
    }

    /** Trang chi tiết giải: hiển thị nội dung, danh sách VĐV/đội và kết quả cơ bản */
    @GetMapping("/tournament/{id}")
    public String tournamentDetail(@PathVariable("id") Integer id, Model model) {
        try (SQLSRVConnectionManager mgr = new SQLSRVConnectionManager()) {
            mgr.setConfig(dbCfg);
            Connection conn = mgr.connect();

            var giaiRepo = new GiaiDauRepository(conn);
            var noiDungRepo = new NoiDungRepository(conn);
            var dkCaNhanRepo = new DangKiCaNhanRepository(conn);
            var vdvRepo = new VanDongVienRepository(conn);
            var dkDoiRepo = new DangKiDoiRepository(conn);
            var kqCaNhanRepo = new KetQuaCaNhanRepository(conn);
            var kqDoiRepo = new KetQuaDoiRepository(conn);

            Optional<GiaiDau> giaiOpt = giaiRepo.findById(id);
            if (giaiOpt.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy giải đấu với ID=" + id);
                return "error";
            }
            GiaiDau giai = giaiOpt.get();

            List<NoiDung> nds = noiDungRepo.findByTournament(giai.getId());
            List<Map<String, Object>> detailBlocks = new ArrayList<>();

            for (NoiDung nd : nds) {
                Map<String, Object> block = new HashMap<>();
                block.put("noiDung", nd);
                boolean isTeam = nd.getTeam() != null && nd.getTeam();
                block.put("isTeam", isTeam);

                if (isTeam) {
                    // Danh sách đội đăng ký
                    block.put("teams", dkDoiRepo.findAllBy(giai.getId(), nd.getId()));
                    // Kết quả đội
                    List<KetQuaDoi> kq = kqDoiRepo.list(giai.getId(), nd.getId());
                    block.put("resultsTeam", kq);
                } else {
                    // Danh sách VĐV đăng ký (kèm tên)
                    var regs = dkCaNhanRepo.list(giai.getId(), nd.getId());
                    List<VanDongVien> vdvList = new ArrayList<>();
                    for (var r : regs) {
                        VanDongVien v = vdvRepo.findById(r.getIdVdv());
                        if (v != null) vdvList.add(v);
                    }
                    block.put("players", vdvList);

                    // Kết quả cá nhân (map sang tên VĐV)
                    List<KetQuaCaNhan> kq = kqCaNhanRepo.list(giai.getId(), nd.getId());
                    List<Map<String, Object>> resultPlayers = new ArrayList<>();
                    for (KetQuaCaNhan r : kq) {
                        VanDongVien v = vdvRepo.findById(r.getIdVdv());
                        Map<String, Object> row = new HashMap<>();
                        row.put("rank", r.getThuHang());
                        row.put("name", v != null ? v.getHoTen() : ("VDV #" + r.getIdVdv()));
                        resultPlayers.add(row);
                    }
                    block.put("resultsPlayer", resultPlayers);
                }

                detailBlocks.add(block);
            }

            // Thống kê cơ bản
            int totalContents = nds.size();
            long totalPlayers = detailBlocks.stream()
                    .filter(b -> !(Boolean) b.get("isTeam"))
                    .mapToLong(b -> {
                        List<?> p = (List<?>) b.getOrDefault("players", List.of());
                        return p.size();
                    }).sum();
            long totalTeams = detailBlocks.stream()
                    .filter(b -> (Boolean) b.get("isTeam"))
                    .mapToLong(b -> {
                        List<?> t = (List<?>) b.getOrDefault("teams", List.of());
                        return t.size();
                    }).sum();

            model.addAttribute("giai", giai);
            model.addAttribute("dateFmt", DATE_FMT);
            model.addAttribute("blocks", detailBlocks);
            model.addAttribute("totalContents", totalContents);
            model.addAttribute("totalPlayers", totalPlayers);
            model.addAttribute("totalTeams", totalTeams);

            return "tournament/tournament-detail";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải chi tiết giải: " + e.getMessage());
            return "exception/error";
        }
    }
}
