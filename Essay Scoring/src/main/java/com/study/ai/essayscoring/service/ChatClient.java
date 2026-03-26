package com.study.ai.essayscoring.service;

/**
 * ChatClient 接口 - 用于AI对话服务
 */
public interface ChatClient {
    /**
     * 调用AI模型
     * @param message 输入消息
     * @return AI返回的响应
     */
    String call(String message);
}
