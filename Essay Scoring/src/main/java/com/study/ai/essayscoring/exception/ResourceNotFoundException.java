package com.study.ai.essayscoring.exception;

/**
 * 资源不存在 → 404。
 *
 * 越权访问（IDOR）也统一抛这个异常，让「资源不存在」与「无权访问」在响应上不可区分，
 * 避免用 403 暴露「该 id 存在」这一信息，从而杜绝 id 枚举。
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
