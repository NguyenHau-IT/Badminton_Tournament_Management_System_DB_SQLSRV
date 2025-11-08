package com.example.btms.web.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Service để load HTML fragments manually
 * Giải quyết vấn đề Thymeleaf fragment resolution không hoạt động
 */
@Service
public class TemplateFragmentService {

    private final SpringTemplateEngine templateEngine;
    private final Map<String, String> fragmentCache = new HashMap<>();

    public TemplateFragmentService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Load HTML fragment từ file
     * 
     * @param fragmentPath Path tương đối từ templates/ (ví dụ: "layouts/header")
     * @param fragmentName Tên fragment (ví dụ: "header")
     * @return HTML content của fragment
     */
    public String loadFragment(String fragmentPath, String fragmentName) {
        String cacheKey = fragmentPath + "::" + fragmentName;
        
        // Check cache first
        if (fragmentCache.containsKey(cacheKey)) {
            return fragmentCache.get(cacheKey);
        }

        try {
            // Load file từ classpath
            ClassPathResource resource = new ClassPathResource("templates/" + fragmentPath + ".html");
            String content = new String(
                Files.readAllBytes(Path.of(resource.getURI())),
                StandardCharsets.UTF_8
            );

            // Extract fragment content
            String fragmentHtml = extractFragment(content, fragmentName);
            
            // Cache result
            fragmentCache.put(cacheKey, fragmentHtml);
            
            return fragmentHtml;
        } catch (IOException e) {
            return "<!-- Fragment not found: " + fragmentPath + " :: " + fragmentName + " -->";
        }
    }

    /**
     * Extract fragment từ HTML content
     * Tìm đoạn HTML giữa th:fragment="name" và closing tag
     */
    private String extractFragment(String html, String fragmentName) {
        // Tìm opening tag với th:fragment
        String fragmentPattern = "th:fragment=\"" + fragmentName + "\"";
        int startIndex = html.indexOf(fragmentPattern);
        
        if (startIndex == -1) {
            return "<!-- Fragment '" + fragmentName + "' not found -->";
        }

        // Tìm opening tag bắt đầu
        int tagStart = html.lastIndexOf('<', startIndex);
        
        // Lấy tag name
        int tagNameEnd = html.indexOf(' ', tagStart);
        if (tagNameEnd == -1) tagNameEnd = html.indexOf('>', tagStart);
        String tagName = html.substring(tagStart + 1, tagNameEnd).trim();
        
        // Tìm closing tag tương ứng
        String closingTag = "</" + tagName + ">";
        int endIndex = html.indexOf(closingTag, startIndex);
        
        if (endIndex == -1) {
            return "<!-- Closing tag not found for fragment '" + fragmentName + "' -->";
        }

        // Extract fragment HTML (bao gồm cả opening và closing tag)
        return html.substring(tagStart, endIndex + closingTag.length());
    }

    /**
     * Clear cache
     */
    public void clearCache() {
        fragmentCache.clear();
    }

    /**
     * Get template engine để render dynamic content
     */
    public String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}
