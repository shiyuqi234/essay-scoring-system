package com.study.ai.essayscoring.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 用于验证安全配置和生成密码哈希
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/h2-status")
    public Map<String, Object> h2Status() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "H2 console should be available at /h2-console");
        result.put("url", "http://localhost:8080/h2-console");
        result.put("jdbcUrl", "jdbc:h2:file:./data/essayscoring");
        result.put("username", "sa");
        result.put("password", "");
        return result;
    }

    @GetMapping("/generate-password")
    public Map<String, Object> generatePassword(@RequestParam(defaultValue = "password123") String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);
        
        Map<String, Object> result = new HashMap<>();
        result.put("originalPassword", password);
        result.put("bcryptHash", hash);
        result.put("sqlExample", String.format("UPDATE users SET password = '%s' WHERE username = 'your_username';", hash));
        return result;
    }
}
