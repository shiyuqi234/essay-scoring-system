package com.study.ai.essayscoring.controller;

import com.study.ai.essayscoring.service.AIScoringService;
import com.study.ai.essayscoring.service.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试/诊断控制器
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired(required = false)
    private ChatClient chatClient;

    @Autowired(required = false)
    private AIScoringService aiScoringService;

    @Value("${spring.cloud.ai.deepseek.api-key:your-api-key-here}")
    private String apiKey;

    @GetMapping("/h2-status")
    public Map<String, Object> h2Status() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "H2 console should be available at /h2-console");
        result.put("url", "http://localhost:8080/h2-console");
        result.put("jdbcUrl", "jdbc:h2:file:./data/essayscoring");
        result.put("username", "sa");
        result.put("password", "");
        return result;
    }

    /**
     * 注意：密码生成端点已禁用（安全原因）。
     * 如需生成 BCrypt 密码哈希，请使用 PasswordGenerator.main() 方法。
     */
    /*
    @GetMapping("/generate-password")
    public Map<String, Object> generatePassword(@RequestParam(defaultValue = "password123") String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);
        Map<String, Object> result = new HashMap<>();
        result.put("originalPassword", password);
        result.put("bcryptHash", hash);
        return result;
    }
    */

    /**
     * AI 诊断接口 — 检查 API Key 和 ChatClient 状态
     */
    @GetMapping("/ai-status")
    public Map<String, Object> aiStatus() {
        Map<String, Object> result = new HashMap<>();

        // API Key 检查（只显示前8位）
        String masked = apiKey;
        if (apiKey != null && apiKey.length() > 8 && !apiKey.equals("your-api-key-here")) {
            masked = apiKey.substring(0, 8) + "***";
        }
        result.put("configuredApiKey", masked);
        result.put("isDefaultKey", "your-api-key-here".equals(apiKey));
        result.put("keyFromEnv", !"your-api-key-here".equals(apiKey) && apiKey != null && !apiKey.isEmpty());

        // ChatClient 类型
        if (chatClient != null) {
            result.put("chatClientClass", chatClient.getClass().getSimpleName());
            result.put("isMock", chatClient.getClass().getSimpleName().contains("Mock"));
        } else {
            result.put("chatClientClass", "null (未注入)");
            result.put("isMock", true);
        }

        // 最近一次错误
        if (aiScoringService != null) {
            result.put("lastAiError", aiScoringService.getLastError() != null ? aiScoringService.getLastError() : "无错误");
        }

        // 快速测试：发送一个简单请求
        if (chatClient != null) {
            try {
                long start = System.currentTimeMillis();
                String testResp = chatClient.call("请回复一个简短的JSON：{\"status\":\"ok\"}，不要多说任何话。");
                long elapsed = System.currentTimeMillis() - start;
                result.put("testCallSuccess", true);
                result.put("testCallMs", elapsed);
                result.put("testCallLength", testResp != null ? testResp.length() : 0);
            } catch (Exception e) {
                result.put("testCallSuccess", false);
                result.put("testCallError", e.getMessage());
            }
        }

        return result;
    }
}
