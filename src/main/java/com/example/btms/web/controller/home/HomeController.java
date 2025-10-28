package com.example.btms.web.controller.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    @GetMapping("/")
    public String landing(Model model, HttpServletRequest request) {
        String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("scrollTarget", "#slide-2");
        return "mainhome/mainhome";
    }
}