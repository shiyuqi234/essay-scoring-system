package com.study.ai.essayscoring.exception;

/**
 * 模型输出被 max_tokens 截断 —— 可重试，与网络失败区分开。
 */
public class TruncatedResponseException extends RuntimeException {
    public TruncatedResponseException(String message) {
        super(message);
    }
}
