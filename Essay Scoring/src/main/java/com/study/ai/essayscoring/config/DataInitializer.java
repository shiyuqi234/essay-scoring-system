package com.study.ai.essayscoring.config;

import com.study.ai.essayscoring.entity.UserAccount;
import com.study.ai.essayscoring.entity.UserRole;
import com.study.ai.essayscoring.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 演示账号初始化器 — 启动时自动创建学生和教师演示账号（仅当不存在时）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createDemoAccount("student1", "123456", UserRole.STUDENT, "张三", "S2024001");
        createDemoAccount("teacher1", "123456", UserRole.TEACHER, "李老师", null);
    }

    private void createDemoAccount(String username, String rawPassword, UserRole role,
                                    String displayName, String studentId) {
        if (userAccountRepository.findByUsername(username).isPresent()) {
            log.info("演示账号已存在，跳过: {}", username);
            return;
        }
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setRole(role);
        account.setDisplayName(displayName);
        account.setStudentId(studentId);
        account.setEnabled(true);
        userAccountRepository.save(account);
        log.info("演示账号创建成功: {} ({})", username, role);
    }
}
