package com.study.ai.essayscoring.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类 - 用于生成 BCrypt 加密的密码
 * 主要用于创建测试数据的 SQL 脚本
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成常用密码的 BCrypt 哈希值
        String[] passwords = {"password123", "student123", "teacher123", "admin123"};
        
        System.out.println("=== BCrypt 密码哈希值 ===");
        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println(password + " => " + hash);
        }
    }
}
