package com.study.ai.essayscoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户账户实体（支持学生、教师角色）
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 登录用户名（唯一）
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 加密后的密码
     */
    @Column(nullable = false)
    private String password;

    /**
     * 角色：STUDENT / TEACHER
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    /**
     * 绑定的学生ID（如果是学生）
     */
    @Column(length = 50)
    private String studentId;

    /**
     * 显示姓名
     */
    @Column(length = 100)
    private String displayName;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}

