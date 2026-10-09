package com.study.ai.essayscoring.exception;

/**
 * 业务校验失败 → 400。
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
