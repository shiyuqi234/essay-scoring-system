package com.study.ai.essayscoring.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 统一 API 响应包装类
 * 替代 Controller 中手动构建 Map<String, Object> 的重复代码
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    private boolean success;
    private String message;
    private Object data;
    private Integer count;
    private String token;
    private String username;
    private String role;
    private String displayName;
    private String studentId;
    private Boolean authenticated;

    // ===== 工厂方法 =====

    public static ApiResponse success() {
        return new ApiResponse(true, "操作成功", null, null, null, null, null, null, null, null);
    }

    public static ApiResponse success(Object data) {
        return new ApiResponse(true, null, data, null, null, null, null, null, null, null);
    }

    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(true, message, data, null, null, null, null, null, null, null);
    }

    public static ApiResponse successWithCount(Object data, int count) {
        return new ApiResponse(true, null, data, count, null, null, null, null, null, null);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(false, message, null, null, null, null, null, null, null, null);
    }

    // ===== 认证专用工厂方法 =====

    public static ApiResponse loginSuccess(String token, String username, String role,
                                            String displayName, String studentId) {
        ApiResponse resp = new ApiResponse(true, null, null, null, token, username, role, displayName, studentId, null);
        return resp;
    }

    public static ApiResponse authStatus(boolean authenticated, String username,
                                          String role, String displayName, String studentId) {
        ApiResponse resp = new ApiResponse(true, null, null, null, null,
                username, role, displayName, studentId, authenticated);
        return resp;
    }
}
