package com.study.ai.essayscoring.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.ai.essayscoring.exception.TruncatedResponseException;
import com.study.ai.essayscoring.service.ChatClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * AI配置类 - 配置 DeepSeek API（OpenAI 兼容接口）
 */
@Configuration
@Slf4j
public class AIConfig {

    @Value("${spring.cloud.ai.deepseek.api-key:your-api-key-here}")
    private String apiKey;

    @Value("${spring.cloud.ai.deepseek.chat.options.model:deepseek-chat}")
    private String model;

    @Value("${spring.cloud.ai.deepseek.chat.options.temperature:0.3}")
    private Float temperature;

    @Value("${spring.cloud.ai.deepseek.chat.options.max-tokens:2000}")
    private Integer maxTokens;

    /**
     * 创建ChatClient Bean
     * 使用 DeepSeek OpenAI 兼容 API。
     *
     * 关键：用 Profile 显式声明意图，而不是用「API Key 是否存在」隐式切换行为。
     * 配置缺失在 dev/test 下允许 Mock（日志醒目）；生产缺 Key 直接 fail-fast 拒绝启动。
     */
    @Bean
    @Primary
    public ChatClient chatClient(ObjectMapper objectMapper, Environment environment) {
        boolean keyMissing = apiKey == null || apiKey.isBlank() || apiKey.equals("your-api-key-here");

        if (keyMissing) {
            if (environment.acceptsProfiles(Profiles.of("dev", "test"))) {
                log.warn("========== 【开发模式】 ==========");
                log.warn("DeepSeek API Key 未配置，使用 MockChatClient。");
                log.warn("返回的是【伪造评分】，仅供界面联调，切勿用于真实数据！");
                log.warn("================================");
                return new MockChatClient();
            }
            // ★ 生产环境：拒绝启动，而不是悄悄用假数据
            throw new IllegalStateException(
                    "缺少 DEEPSEEK_API_KEY。生产环境不允许以 Mock 模式运行，"
                            + "请配置环境变量，或使用 -Dspring.profiles.active=dev 在本地开发。");
        }

        log.info("使用 DeepSeek API，模型: {}", model);
        return new DeepSeekChatClient(apiKey, model, temperature, maxTokens, objectMapper);
    }

    /**
     * DeepSeek ChatClient 实现（OpenAI 兼容接口）
     */
    public static class DeepSeekChatClient implements ChatClient {
        private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";

        private final String apiKey;
        private final String model;
        private final Float temperature;
        private final Integer maxTokens;
        private final RestTemplate restTemplate;
        private final ObjectMapper objectMapper;

        public DeepSeekChatClient(String apiKey, String model, Float temperature, Integer maxTokens,
                                  ObjectMapper objectMapper) {
            this.apiKey = apiKey;
            this.model = model;
            this.temperature = temperature;
            this.maxTokens = maxTokens;
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(10_000);  // 10 秒连接超时
            factory.setReadTimeout(60_000);     // 60 秒读取超时
            this.restTemplate = new RestTemplate(factory);
            this.objectMapper = objectMapper;
        }

        @Override
        public String call(String message) {
            try {
                // 构建请求体
                DeepSeekRequest request = new DeepSeekRequest();
                request.setModel(model);
                request.setMessages(Collections.singletonList(
                        new Message("user", message)
                ));
                request.setTemperature(temperature);
                request.setMaxTokens(maxTokens);

                // 构建请求头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                String requestBody = objectMapper.writeValueAsString(request);
                // ★ 完整内容只进 trace；debug 只打长度，避免学生作文原文进日志
                log.debug("DeepSeek API 请求: {} 字符", requestBody.length());
                log.trace("DeepSeek API 请求体: {}", requestBody);

                HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(
                        API_URL, entity, String.class);

                String responseBody = response.getBody();
                log.debug("DeepSeek API 响应: {} 字符",
                        responseBody != null ? responseBody.length() : 0);
                log.trace("DeepSeek API 响应体: {}", responseBody);

                if (responseBody != null) {
                    DeepSeekResponse deepSeekResponse = objectMapper.readValue(
                            responseBody, DeepSeekResponse.class);
                    if (deepSeekResponse.getChoices() != null
                            && !deepSeekResponse.getChoices().isEmpty()) {
                        Choice choice = deepSeekResponse.getChoices().get(0);

                        // ★ 先判截断，再取内容
                        if ("length".equals(choice.getFinishReason())) {
                            throw new TruncatedResponseException(
                                    "模型输出被 max_tokens(" + maxTokens + ") 截断，无法得到完整 JSON");
                        }
                        if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                            // ★ Token 消耗埋点：记录输入/输出 token，便于成本与用量观测
                            if (deepSeekResponse.getUsage() != null) {
                                Usage usage = deepSeekResponse.getUsage();
                                log.info("DeepSeek Token 消耗: prompt={}, completion={}, total={}",
                                        usage.getPromptTokens(), usage.getCompletionTokens(),
                                        usage.getTotalTokens());
                            }
                            return choice.getMessage().getContent();
                        }
                    }
                }

                log.warn("DeepSeek API 返回空结果");
                // ★ 抛异常让调用方明确知道失败，而不是把错误伪装成正常返回值
                throw new IllegalStateException("DeepSeek API 返回空结果");
            } catch (TruncatedResponseException e) {
                throw e;
            } catch (Exception e) {
                log.error("调用 DeepSeek API 失败", e);
                throw new RuntimeException("调用 DeepSeek API 失败: " + e.getMessage(), e);
            }
        }

        // --- 请求/响应模型 ---

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class DeepSeekRequest {
            private String model;
            private List<Message> messages;
            private Float temperature;
            @JsonProperty("max_tokens")
            private Integer maxTokens;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Message {
            private String role;
            private String content;

            public Message() {}

            public Message(String role, String content) {
                this.role = role;
                this.content = content;
            }
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class DeepSeekResponse {
            private List<Choice> choices;
            private Usage usage;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Usage {
            @JsonProperty("prompt_tokens")
            private Integer promptTokens;
            @JsonProperty("completion_tokens")
            private Integer completionTokens;
            @JsonProperty("total_tokens")
            private Integer totalTokens;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Choice {
            private Message message;
            @JsonProperty("finish_reason")
            private String finishReason;      // ★ "stop" = 正常结束，"length" = 被截断
        }
    }

    /**
     * 模拟ChatClient（用于测试，当API Key未配置时）
     */
    public static class MockChatClient implements ChatClient {
        @Override
        public String call(String message) {
            // 模拟AI返回的评分+反馈结果
            return "{"
                + "\"contentScore\": 25.0,"
                + "\"structureScore\": 20.0,"
                + "\"languageScore\": 22.0,"
                + "\"creativityScore\": 18.0,"
                + "\"contentComment\": \"内容充实，能围绕主题展开，举例具体生动，这一点做得很好！如果能再增加一两个细节描写，文章会更有画面感。\","
                + "\"structureComment\": \"整体结构清晰，开头点题、中间展开、结尾总结的框架很完整。段落之间的过渡可以再自然一些。\","
                + "\"languageComment\": \"语言表达流畅自然，用词基本准确。有些句子较长，建议适当拆分，让节奏更明快。\","
                + "\"creativityComment\": \"你的观察角度有自己的特色，不是人云亦云。不妨试着用更独特的比喻来表达你的想法。\","
                + "\"overallComment\": \"这是一篇有温度、有思考的作文，老师读完后能感受到你的认真。继续坚持多读多写，你的文字会越来越有力量！\","
                + "\"feedbacks\": ["
                + "  {"
                + "    \"feedbackType\": \"亮点表扬\","
                + "    \"position\": \"全文\","
                + "    \"issue\": \"你能够围绕主题展开写作，内容充实，表达真诚。\","
                + "    \"suggestion\": \"继续保持这种认真写作的态度，每次写作都像这样用心。\","
                + "    \"severity\": \"低\""
                + "  },"
                + "  {"
                + "    \"feedbackType\": \"表达优化\","
                + "    \"position\": \"第1段\","
                + "    \"issue\": \"开头可以更具吸引力，目前比较平铺直叙。\","
                + "    \"suggestion\": \"不妨用一个有趣的问题或生动的场景来开头，一下子抓住读者的注意力。\","
                + "    \"improvedExample\": \"你可曾想过，一件看似平常的小事，却让我记到了今天……\","
                + "    \"severity\": \"中\""
                + "  },"
                + "  {"
                + "    \"feedbackType\": \"结构建议\","
                + "    \"position\": \"第2段\","
                + "    \"issue\": \"段落较长，包含多个意思，可以拆分让层次更清楚。\","
                + "    \"suggestion\": \"一个段落尽量只讲一个意思，讲清楚了再另起一段。\","
                + "    \"improvedExample\": \"把'虽然……但是……'的转折部分单独成段，效果会更好。\","
                + "    \"severity\": \"中\""
                + "  }"
                + "]"
                + "}";
        }
    }
}
