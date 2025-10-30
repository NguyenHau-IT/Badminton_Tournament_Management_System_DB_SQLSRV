package com.example.btms.web.controller.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String landing(Model model, HttpServletRequest request) {
        String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
        model.addAttribute("baseUrl", baseUrl);
        List<String> scrollTargets = Arrays.asList("#slide-1", "#slide-2", "#slide-3");
        model.addAttribute("scrollTargets", scrollTargets);
        return "home/home";
    }
}       