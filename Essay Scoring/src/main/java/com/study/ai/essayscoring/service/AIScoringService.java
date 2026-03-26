package com.study.ai.essayscoring.service;

import com.study.ai.essayscoring.entity.Essay;
import com.study.ai.essayscoring.entity.Feedback;
import com.study.ai.essayscoring.entity.Score;
import com.study.ai.essayscoring.repository.ScoringRuleRepository;
import com.study.ai.essayscoring.service.ChatClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI评分服务 - 集成阿里云百炼平台
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIScoringService {
    
    private final ChatClient chatClient;
    private final ScoringRuleRepository scoringRuleRepository;
    
    /**
     * 对作文进行AI评分
     */
    public Score scoreEssay(Essay essay) {
        log.info("开始对作文进行AI评分，作文ID: {}", essay.getId());
        
        try {
            // 构建评分提示词
            String prompt = buildScoringPrompt(essay);
            
            // 调用AI模型进行评分
            String aiResponse = chatClient.call(prompt);
            log.info("AI评分响应: {}", aiResponse);
            
            // 解析AI返回的评分结果
            Score score = parseScoringResult(aiResponse, essay);
            
            return score;
            
        } catch (Exception e) {
            log.error("AI评分失败", e);
            // 返回默认评分
            return createDefaultScore(essay);
        }
    }
    
    /**
     * 生成反馈建议
     */
    public List<Feedback> generateFeedbacks(Essay essay, Score score) {
        log.info("开始生成反馈建议，作文ID: {}", essay.getId());
        
        List<Feedback> feedbacks = new ArrayList<>();
        
        try {
            // 构建反馈生成提示词
            String prompt = buildFeedbackPrompt(essay, score);
            
            // 调用AI模型生成反馈
            String aiResponse = chatClient.call(prompt);
            log.info("AI反馈响应: {}", aiResponse);
            
            // 解析反馈结果
            feedbacks = parseFeedbackResult(aiResponse, essay);
            
        } catch (Exception e) {
            log.error("生成反馈失败", e);
        }
        
        return feedbacks;
    }
    
    /**
     * 构建评分提示词
     */
    private String buildScoringPrompt(Essay essay) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位经验丰富的语文教师，请对以下学生作文进行专业评分。\n\n");
        prompt.append("【作文信息】\n");
        prompt.append("标题：").append(essay.getTitle()).append("\n");
        prompt.append("年级：").append(essay.getGrade()).append("\n");
        prompt.append("文体：").append(essay.getEssayType()).append("\n");
        prompt.append("字数：").append(essay.getWordCount()).append("\n\n");
        prompt.append("【作文内容】\n");
        prompt.append(essay.getContent()).append("\n\n");
        prompt.append("【评分要求】\n");
        prompt.append("请从以下四个维度进行评分，每个维度给出具体分数和评价：\n");
        prompt.append("1. 内容（满分30分）：主题明确、内容充实、观点正确\n");
        prompt.append("2. 结构（满分25分）：层次清晰、逻辑严密、结构完整\n");
        prompt.append("3. 语言（满分25分）：表达准确、用词恰当、语句流畅\n");
        prompt.append("4. 创意（满分20分）：立意新颖、角度独特、富有想象力\n\n");
        prompt.append("【输出格式】\n");
        prompt.append("请严格按照以下JSON格式输出评分结果：\n");
        prompt.append("{\n");
        prompt.append("  \"contentScore\": 分数(0-30),\n");
        prompt.append("  \"structureScore\": 分数(0-25),\n");
        prompt.append("  \"languageScore\": 分数(0-25),\n");
        prompt.append("  \"creativityScore\": 分数(0-20),\n");
        prompt.append("  \"contentComment\": \"内容评价\",\n");
        prompt.append("  \"structureComment\": \"结构评价\",\n");
        prompt.append("  \"languageComment\": \"语言评价\",\n");
        prompt.append("  \"creativityComment\": \"创意评价\",\n");
        prompt.append("  \"overallComment\": \"综合评语\"\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    /**
     * 构建反馈生成提示词
     */
    private String buildFeedbackPrompt(Essay essay, Score score) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请针对以下作文提供具体的改进建议和优化示例。\n\n");
        prompt.append("【作文内容】\n");
        prompt.append(essay.getContent()).append("\n\n");
        prompt.append("【当前评分】\n");
        prompt.append("总分：").append(score.getTotalScore()).append("分\n");
        prompt.append("内容：").append(score.getContentScore()).append("分 - ").append(score.getContentComment()).append("\n");
        prompt.append("结构：").append(score.getStructureScore()).append("分 - ").append(score.getStructureComment()).append("\n");
        prompt.append("语言：").append(score.getLanguageScore()).append("分 - ").append(score.getLanguageComment()).append("\n");
        prompt.append("创意：").append(score.getCreativityScore()).append("分 - ").append(score.getCreativityComment()).append("\n\n");
        prompt.append("【反馈要求】\n");
        prompt.append("请识别作文中的问题（语法错误、表达不当、结构问题、词汇使用等），\n");
        prompt.append("为每个问题提供：问题描述、改进建议、优化示例。\n\n");
        prompt.append("【输出格式】\n");
        prompt.append("请按照以下格式输出，每个反馈一行：\n");
        prompt.append("类型|位置|问题|建议|示例|严重程度\n");
        prompt.append("例如：语法错误|第3段|主谓不一致|建议修改为...|优化后：...|中\n");
        
        return prompt.toString();
    }
    
    /**
     * 解析评分结果
     */
    private Score parseScoringResult(String aiResponse, Essay essay) {
        Score score = new Score();
        score.setEssay(essay);
        score.setModelVersion("qwen-turbo");
        
        try {
            // 尝试从JSON中提取分数
            Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*([0-9.]+|\"[^\"]+\")");
            Matcher matcher = pattern.matcher(aiResponse);
            
            while (matcher.find()) {
                String key = matcher.group(1);
                String value = matcher.group(2).replace("\"", "");
                
                switch (key) {
                    case "contentScore":
                        score.setContentScore(Double.parseDouble(value));
                        break;
                    case "structureScore":
                        score.setStructureScore(Double.parseDouble(value));
                        break;
                    case "languageScore":
                        score.setLanguageScore(Double.parseDouble(value));
                        break;
                    case "creativityScore":
                        score.setCreativityScore(Double.parseDouble(value));
                        break;
                    case "contentComment":
                        score.setContentComment(value);
                        break;
                    case "structureComment":
                        score.setStructureComment(value);
                        break;
                    case "languageComment":
                        score.setLanguageComment(value);
                        break;
                    case "creativityComment":
                        score.setCreativityComment(value);
                        break;
                    case "overallComment":
                        score.setOverallComment(value);
                        break;
                }
            }
            
            // 计算总分
            if (score.getContentScore() != null && score.getStructureScore() != null &&
                score.getLanguageScore() != null && score.getCreativityScore() != null) {
                double total = score.getContentScore() + score.getStructureScore() +
                              score.getLanguageScore() + score.getCreativityScore();
                score.setTotalScore(total);
            }
            
            // 如果没有解析到数据，使用默认值
            if (score.getTotalScore() == null) {
                return createDefaultScore(essay);
            }
            
        } catch (Exception e) {
            log.error("解析评分结果失败", e);
            return createDefaultScore(essay);
        }
        
        return score;
    }
    
    /**
     * 解析反馈结果
     */
    private List<Feedback> parseFeedbackResult(String aiResponse, Essay essay) {
        List<Feedback> feedbacks = new ArrayList<>();
        
        try {
            String[] lines = aiResponse.split("\n");
            for (String line : lines) {
                if (line.trim().isEmpty() || !line.contains("|")) {
                    continue;
                }
                
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    Feedback feedback = new Feedback();
                    feedback.setEssay(essay);
                    feedback.setFeedbackType(parts[0].trim());
                    feedback.setPosition(parts.length > 1 ? parts[1].trim() : "");
                    feedback.setIssue(parts.length > 2 ? parts[2].trim() : "");
                    feedback.setSuggestion(parts.length > 3 ? parts[3].trim() : "");
                    feedback.setImprovedExample(parts.length > 4 ? parts[4].trim() : "");
                    feedback.setSeverity(parts.length > 5 ? parts[5].trim() : "中");
                    
                    feedbacks.add(feedback);
                }
            }
        } catch (Exception e) {
            log.error("解析反馈结果失败", e);
        }
        
        // 如果解析失败，至少返回一个通用反馈
        if (feedbacks.isEmpty()) {
            Feedback defaultFeedback = new Feedback();
            defaultFeedback.setEssay(essay);
            defaultFeedback.setFeedbackType("综合建议");
            defaultFeedback.setIssue("请继续努力提高作文质量");
            defaultFeedback.setSuggestion("多阅读优秀范文，注意语言表达的准确性和结构的完整性");
            defaultFeedback.setSeverity("低");
            feedbacks.add(defaultFeedback);
        }
        
        return feedbacks;
    }
    
    /**
     * 创建默认评分（当AI调用失败时使用）
     */
    private Score createDefaultScore(Essay essay) {
        Score score = new Score();
        score.setEssay(essay);
        score.setContentScore(20.0);
        score.setStructureScore(18.0);
        score.setLanguageScore(18.0);
        score.setCreativityScore(15.0);
        score.setTotalScore(71.0);
        score.setContentComment("内容基本完整，但可以更加充实");
        score.setStructureComment("结构基本清晰，但逻辑可以更严密");
        score.setLanguageComment("语言表达基本准确，但可以更加流畅");
        score.setCreativityComment("创意一般，可以尝试更独特的视角");
        score.setOverallComment("作文整体质量良好，有进一步提升的空间");
        score.setModelVersion("default");
        return score;
    }
}
