package com.study.ai.essayscoring.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    /**
     * 学生ID（注册接口只能创建学生账号，教师账号由管理员创建）
     */
    private String studentId;

    /**
     * 显示姓名
     */
    private String displayName;
}
