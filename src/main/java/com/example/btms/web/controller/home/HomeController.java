package com.example.btms.web.controller.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller xử lý trang chủ và các trang landing
 * Route:
 *   - / : Trang chủ (landing page)
 *   - /home : Alias cho trang chủ
 */
@Controller
public class HomeController {

    /**
     * Hiển thị trang chủ (landing page)
     * 
     * @param model Model để truyền dữ liệu xuống view
     * @return Template path: main-home/main-home
     */
    @GetMapping({"/", "/home"})
    public String showHome(Model model) {
        // Thêm các thông số thống kê cho stats section
        model.addAttribute("totalTournaments", 500);
        model.addAttribute("totalPlayers", 10000);
        model.addAttribute("totalClubs", 150);
        model.addAttribute("totalMatches", 25000);
        model.addAttribute("growthRate", 35);
        
        // Thêm thông tin phiên bản app
        model.addAttribute("appVersion", "1.0.0");
        model.addAttribute("releaseDate", "Tháng 11, 2025");
        
        // Thêm metadata cho SEO
        model.addAttribute("pageTitle", "BTMS - Hệ thống Quản lý Giải đấu Cầu lông Chuyên nghiệp");
        model.addAttribute("pageDescription", 
            "BTMS - Hệ thống quản lý giải đấu cầu lông hiện đại với điều khiển từ xa, " +
            "real-time scoring và quản lý đa sân chuyên nghiệp. Miễn phí 100%!");
        model.addAttribute("pageKeywords", 
            "cầu lông, giải đấu, quản lý giải đấu, badminton, tournament, BTMS, " +
            "real-time scoring, điều khiển từ xa, quản lý sân");
        
        // Thêm flag để highlight menu item
        model.addAttribute("activePage", "home");
        
        return "main-home/main-home";
    }
    
    /**
     * Endpoint để kiểm tra health của controller
     * Useful cho testing và monitoring
     * 
     * @return Simple text response
     */
    @GetMapping("/health")
    public String health(Model model) {
        model.addAttribute("status", "OK");
        model.addAttribute("timestamp", System.currentTimeMillis());
        model.addAttribute("message", "HomeController is running");
        
        // Có thể tạo template riêng cho health check hoặc return JSON
        // Tạm thời redirect về home
        return "redirect:/";
    }
}
