package com.study.ai.essayscoring.config;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.study.ai.essayscoring.service.ChatClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

/**
 * AI配置类 - 配置阿里云百炼平台（使用DashScope SDK）
 */
@Configuration
@Slf4j
public class AIConfig {
    
    @Value("${spring.cloud.ai.dashscope.api-key:your-api-key-here}")
    private String apiKey;
    
    @Value("${spring.cloud.ai.dashscope.chat.options.model:qwen-turbo}")
    private String model;
    
    @Value("${spring.cloud.ai.dashscope.chat.options.temperature:0.3}")
    private Float temperature;
    
    @Value("${spring.cloud.ai.dashscope.chat.options.max-tokens:2000}")
    private Integer maxTokens;
    
    /**
     * 创建ChatClient Bean
     * 使用阿里云DashScope SDK
     */
    @Bean
    @Primary
    public ChatClient chatClient() {
        // 如果API Key未配置，使用模拟客户端
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-api-key-here")) {
            return new MockChatClient();
        }
        
        // 使用DashScope SDK创建真实的ChatClient
        return new DashScopeChatClient(apiKey, model, temperature, maxTokens);
    }
    
    /**
     * DashScope ChatClient实现
     */
    public static class DashScopeChatClient implements ChatClient {
        private final Generation gen;
        private final String apiKey;
        private final String model;
        private final Float temperature;
        private final Integer maxTokens;
        
        public DashScopeChatClient(String apiKey, String model, Float temperature, Integer maxTokens) {
            this.apiKey = apiKey;
            this.model = model;
            this.temperature = temperature;
            this.maxTokens = maxTokens;
            this.gen = new Generation();
        }
        
        @Override
        public String call(String message) {
            try {
                List<Message> messages = new ArrayList<>();
                messages.add(Message.builder()
                        .role(Role.USER.getValue())
                        .content(message)
                        .build());
                
                QwenParam param = QwenParam.builder()
                        .apiKey(apiKey)
                        .model(model)
                        .messages(messages)
                        .temperature(temperature)
                        .maxTokens(maxTokens)
                        .resultFormat(QwenParam.ResultFormat.MESSAGE)
                        .build();
                
                var result = gen.call(param);
                
                if (result != null && result.getOutput() != null && 
                    result.getOutput().getChoices() != null && 
                    !result.getOutput().getChoices().isEmpty()) {
                    return result.getOutput().getChoices().get(0).getMessage().getContent();
                }
                
                log.warn("DashScope API调用返回空结果");
                return "AI调用失败，未返回有效结果";
            } catch (Exception e) {
                log.error("调用DashScope API失败", e);
                throw new RuntimeException("调用DashScope API失败: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 模拟ChatClient（用于测试，当API Key未配置时）
     */
    public static class MockChatClient implements ChatClient {
        @Override
        public String call(String message) {
            // 模拟AI返回的评分结果
            if (message.contains("评分")) {
                return "{\"contentScore\": 25.0, \"structureScore\": 20.0, \"languageScore\": 22.0, \"creativityScore\": 18.0, \"contentComment\": \"内容充实，主题明确\", \"structureComment\": \"结构清晰，层次分明\", \"languageComment\": \"语言流畅，表达准确\", \"creativityComment\": \"有一定创意\", \"overallComment\": \"作文整体质量良好\"}";
            } else {
                return "语法错误|第2段|主谓不一致|建议修改主语和谓语的搭配|优化后：学生们正在认真听讲|中\n表达优化|第3段|用词重复|建议使用同义词替换|优化后：运用更加丰富的词汇|低";
            }
        }
    }
}
