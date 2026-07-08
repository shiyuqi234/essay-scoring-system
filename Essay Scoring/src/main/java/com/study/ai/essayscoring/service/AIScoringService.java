package com.study.ai.essayscoring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.ai.essayscoring.entity.Essay;
import com.study.ai.essayscoring.entity.Feedback;
import com.study.ai.essayscoring.entity.Score;
import com.study.ai.essayscoring.entity.ScoringRule;
import com.study.ai.essayscoring.repository.ScoringRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI评分服务 — 集成 DeepSeek API
 * 评分 + 反馈合为一次调用，Prompt 强调人性化、鼓励式评价
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIScoringService {

    private final ChatClient chatClient;
    private final ScoringRuleRepository scoringRuleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.cloud.ai.deepseek.chat.options.model:deepseek-chat}")
    private String modelVersion;

    /** 默认四维度评分标准 — 教师未配置规则时使用 */
    private static final String[][] DEFAULT_DIMENSIONS = {
        {"内容", "30", "主题是否明确、内容是否充实具体、观点是否正确积极、材料选择是否恰当"},
        {"结构", "25", "层次是否清晰分明、逻辑是否严密连贯、开头结尾是否有力、段落过渡是否自然"},
        {"语言", "25", "表达是否准确流畅、用词是否恰当得体、语句是否通顺、有无语法错误"},
        {"创意", "20", "立意是否新颖独特、角度是否与众不同、想象力是否丰富、有无独到见解"}
    };

    // ──────── 评分 + 反馈（合并为一次调用）────────

    /**
     * 评分结果复合对象（Score + List<Feedback>），避免线程安全问题
     */
    public static class ScoringResult {
        private final Score score;
        private final List<Feedback> feedbacks;

        public ScoringResult(Score score, List<Feedback> feedbacks) {
            this.score = score;
            this.feedbacks = feedbacks;
        }

        public Score getScore() { return score; }
        public List<Feedback> getFeedbacks() { return feedbacks; }
    }

    // 上一次 AI 调用的错误信息（供诊断用）
    private volatile String lastError = null;

    public String getLastError() { return lastError; }

    public ScoringResult scoreEssay(Essay essay) {
        log.info("AI评分开始，essayId={}", essay.getId());
        try {
            String prompt = buildUnifiedPrompt(essay);
            log.info("Prompt构建完成 ({} 字符)，准备调用 ChatClient...", prompt.length());
            String aiResponse = chatClient.call(prompt);
            log.info("AI响应长度: {} 字符", aiResponse != null ? aiResponse.length() : 0);

            ScoringResult result = parseUnifiedResponse(aiResponse, essay);
            lastError = null;
            return result;
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null) msg = e.getClass().getSimpleName();
            // 递归取根因
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            String rootMsg = root.getMessage() != null ? root.getMessage() : root.getClass().getSimpleName();
            lastError = msg + " | 根因: " + rootMsg;
            log.error("AI评分失败: {}", lastError, e);
            List<Feedback> fallbackFeedbacks = createFallbackFeedbacks(essay);
            Score fallback = createFallbackScore(essay);
            // 把错误信息写入综合评语，让用户能看到
            fallback.setOverallComment("【AI调用失败，以下为降级评分】\n错误原因：" + lastError + "\n\n" + fallback.getOverallComment());
            return new ScoringResult(fallback, fallbackFeedbacks);
        }
    }

    // ──────── Prompt 构建 ────────

    /**
     * 构建统一的评分+反馈 Prompt
     */
    private String buildUnifiedPrompt(Essay essay) {
        // 尝试加载教师配置的规则，失败则使用默认维度
        List<ScoringRule> rules;
        try {
            rules = scoringRuleRepository
                    .findByApplicableGradeAndApplicableEssayTypeAndEnabledTrue(
                            essay.getGrade(), essay.getEssayType());
        } catch (Exception e) {
            log.warn("加载评分规则失败，使用默认维度: {}", e.getMessage());
            rules = List.of();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("你是一位温和而严格的语文老师。请认真阅读下面的学生作文，\n");
        sb.append("从四个维度打分，并给出鼓励式的评语和改进建议。\n\n");

        sb.append("【作文信息】\n");
        sb.append("标题：《").append(essay.getTitle()).append("》\n");
        sb.append("年级：").append(essay.getGrade()).append("\n");
        sb.append("文体：").append(essay.getEssayType()).append("\n");
        sb.append("字数：").append(essay.getWordCount() != null ? essay.getWordCount() : "未知").append("\n\n");

        sb.append("【作文正文】\n");
        sb.append(essay.getContent()).append("\n\n");

        // 评分维度与标准
        sb.append("【评分维度与标准】\n");
        if (!rules.isEmpty()) {
            for (int i = 0; i < rules.size(); i++) {
                ScoringRule r = rules.get(i);
                sb.append(i + 1).append(". ").append(r.getDimension())
                        .append("（权重").append(r.getWeight()).append("）：")
                        .append(r.getCriteria()).append("\n");
            }
        } else {
            for (String[] dim : DEFAULT_DIMENSIONS) {
                sb.append("· ").append(dim[0]).append("（满分").append(dim[1]).append("分）：")
                        .append(dim[2]).append("\n");
            }
        }
        sb.append("\n");

        // 评分态度指引
        sb.append("【评分态度 — 非常重要，请严格遵守】\n");
        sb.append("1. 你的评价要像一位关心学生的老师，语言温暖、直白、说人话，不要冷冰冰的套话。\n");
        sb.append("2. 每个维度的评语里，都要先说优点（哪怕只是一点点进步），再说可以改进的地方。\n");
        sb.append("3. 批评和建议要委婉，用「如果能…会更好」「建议试试…」「不妨…」这样的表达。\n");
        sb.append("4. 综合评语要对学生有鼓励，哪怕分数不高，也要让学生感到被尊重、有方向。\n");
        sb.append("5. 反馈建议要具体，指出原文中的问题时给出明确的修改示例。\n\n");

        // 输出格式
        sb.append("【输出格式 — 请严格输出以下JSON，不要有多余文字】\n");
        sb.append("{\n");
        sb.append("  \"contentScore\": 数字(0-30),\n");
        sb.append("  \"structureScore\": 数字(0-25),\n");
        sb.append("  \"languageScore\": 数字(0-25),\n");
        sb.append("  \"creativityScore\": 数字(0-20),\n");
        sb.append("  \"contentComment\": \"先夸优点再说不足，语言温暖直白\",\n");
        sb.append("  \"structureComment\": \"同上\",\n");
        sb.append("  \"languageComment\": \"同上\",\n");
        sb.append("  \"creativityComment\": \"同上\",\n");
        sb.append("  \"overallComment\": \"综合总评，鼓励为主，指出1-2个最值得改进的方向\",\n");
        sb.append("  \"feedbacks\": [\n");
        sb.append("    {\n");
        sb.append("      \"feedbackType\": \"语法错误/表达优化/结构建议/词汇建议/亮点表扬\",\n");
        sb.append("      \"position\": \"第X段 或 开头/结尾/全文\",\n");
        sb.append("      \"issue\": \"原文中的具体问题（引用原文句子）\",\n");
        sb.append("      \"suggestion\": \"具体怎么改，用直白的话说\",\n");
        sb.append("      \"improvedExample\": \"修改后的优化版本\",\n");
        sb.append("      \"severity\": \"高/中/低\"\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n");
        sb.append("\n注意：feedbacks 至少要有2条，必须包含至少1条亮点表扬（feedbackType为\"亮点表扬\"）。");

        return sb.toString();
    }

    // ──────── 响应解析 ────────

    /**
     * 解析统一的评分+反馈 JSON 响应
     */
    private ScoringResult parseUnifiedResponse(String aiResponse, Essay essay) {
        try {
            // 提取 JSON 块（AI 可能在 JSON 外包裹说明文字）
            String json = extractJson(aiResponse);
            JsonNode root = objectMapper.readTree(json);

            Score score = new Score();
            score.setEssay(essay);
            score.setModelVersion(modelVersion);

            score.setContentScore(getDouble(root, "contentScore", 20.0));
            score.setStructureScore(getDouble(root, "structureScore", 18.0));
            score.setLanguageScore(getDouble(root, "languageScore", 18.0));
            score.setCreativityScore(getDouble(root, "creativityScore", 15.0));
            score.setContentComment(getString(root, "contentComment", "内容方面有待提升"));
            score.setStructureComment(getString(root, "structureComment", "结构方面可以优化"));
            score.setLanguageComment(getString(root, "languageComment", "语言表达可以更流畅"));
            score.setCreativityComment(getString(root, "creativityComment", "创意方面可以更大胆"));
            score.setOverallComment(getString(root, "overallComment", "继续加油，你的作文会越来越好！"));

            double total = score.getContentScore() + score.getStructureScore()
                    + score.getLanguageScore() + score.getCreativityScore();
            score.setTotalScore(Math.round(total * 10.0) / 10.0);

            // 解析 feedbacks
            List<Feedback> feedbacks = new ArrayList<>();
            JsonNode fbArray = root.get("feedbacks");
            if (fbArray != null && fbArray.isArray()) {
                for (JsonNode fbNode : fbArray) {
                    Feedback fb = new Feedback();
                    fb.setEssay(essay);
                    fb.setFeedbackType(getString(fbNode, "feedbackType", "综合建议"));
                    fb.setPosition(getString(fbNode, "position", ""));
                    fb.setIssue(getString(fbNode, "issue", ""));
                    fb.setSuggestion(getString(fbNode, "suggestion", ""));
                    fb.setImprovedExample(getString(fbNode, "improvedExample", ""));
                    fb.setSeverity(getString(fbNode, "severity", "中"));
                    feedbacks.add(fb);
                }
            }

            return new ScoringResult(score, feedbacks);

        } catch (Exception e) {
            log.error("解析AI响应失败，使用降级分数。原始响应: {}", aiResponse, e);
            return new ScoringResult(createFallbackScore(essay), createFallbackFeedbacks(essay));
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private double getDouble(JsonNode node, String key, double defaultVal) {
        JsonNode val = node.get(key);
        if (val != null && val.isNumber()) return val.asDouble();
        return defaultVal;
    }

    private String getString(JsonNode node, String key, String defaultVal) {
        JsonNode val = node.get(key);
        if (val != null && val.isTextual()) return val.asText();
        return defaultVal;
    }

    // ──────── 降级方案 ────────

    private Score createFallbackScore(Essay essay) {
        Score score = new Score();
        score.setEssay(essay);
        score.setContentScore(22.0);
        score.setStructureScore(20.0);
        score.setLanguageScore(20.0);
        score.setCreativityScore(16.0);
        score.setTotalScore(78.0);
        score.setContentComment("内容有一定的基础，如果能围绕主题补充更具体的事例会更有说服力。");
        score.setStructureComment("整体框架是清楚的，建议在段落之间加一些过渡句，让文章更连贯。");
        score.setLanguageComment("语言基本通顺，如果能多用一些生动的词语和修辞手法，文章会更有感染力。");
        score.setCreativityComment("想法是好的，不妨试着从一个独特的小角度切入，写出和别人不一样的东西。");
        score.setOverallComment("你的作文已经有了不错的底子，继续加油！多读多写，你一定会越写越好的。");
        score.setModelVersion("fallback");
        return score;
    }

    private List<Feedback> createFallbackFeedbacks(Essay essay) {
        List<Feedback> list = new ArrayList<>();
        Feedback fb1 = new Feedback();
        fb1.setEssay(essay);
        fb1.setFeedbackType("亮点表扬");
        fb1.setPosition("全文");
        fb1.setIssue("你能够围绕主题展开写作，这是非常好的习惯。");
        fb1.setSuggestion("继续保持，每次写作都先想清楚要表达什么。");
        fb1.setSeverity("低");
        list.add(fb1);

        Feedback fb2 = new Feedback();
        fb2.setEssay(essay);
        fb2.setFeedbackType("表达优化");
        fb2.setPosition("第1段");
        fb2.setIssue("开头可以更吸引人一些，目前比较平铺直叙。");
        fb2.setSuggestion("不妨用一个有趣的问题或生动的场景来开头，一下子抓住读者的注意力。");
        fb2.setImprovedExample("你可曾想过，一件看似平常的小事，却让我记到了今天……");
        fb2.setSeverity("中");
        list.add(fb2);

        return list;
    }
}
