package com.study.ai.essayscoring.config;

import com.study.ai.essayscoring.dto.ApiResponse;
import com.study.ai.essayscoring.exception.BusinessException;
import com.study.ai.essayscoring.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

/**
 * 全局异常处理器 — 统一处理 Controller 层抛出的异常，替代重复的 try-catch。
 *
 * 分三层映射：参数校验 / 业务校验 → 400；资源不存在（含越权）→ 404；兜底 → 500 + traceId。
 * 关键：兜底不再把 e.getMessage() 返回给客户端，避免泄漏 SQL、表名、类名等信息。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 参数校验失败 → 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", msg);
        return ResponseEntity.badRequest().body(ApiResponse.error(msg));
    }

    /**
     * 资源不存在 → 404（越权也走这里，故意不区分，避免暴露「该 id 存在」）
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(ResourceNotFoundException e) {
        log.warn("资源不存在: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
    }

    /**
     * 业务校验失败 → 400
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse> handleBusiness(BusinessException e) {
        log.warn("业务校验失败: {}", e.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }

    /**
     * 兜底 → 500。
     * ★ 不再把 e.getMessage() 返回给客户端，改为返回可追溯的 traceId；
     *   详细信息只进服务端日志，用户按 traceId 即可定位。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception e) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        log.error("请求处理异常 traceId={}", traceId, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("服务内部错误，请稍后重试（错误码 " + traceId + "）"));
    }
}
