package com.study.ai.essayscoring.dto;

import com.study.ai.essayscoring.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    /**
     * 角色：STUDENT 或 TEACHER
     */
    @NotNull
    private UserRole role;

    /**
     * 学生ID（仅学生角色需要）
     */
    private String studentId;

    /**
     * 显示姓名
     */
    private String displayName;
}

