package com.study.ai.essayscoring.service;

import com.study.ai.essayscoring.dto.EssaySubmitDTO;
import com.study.ai.essayscoring.dto.ScoreUpdateRequest;
import com.study.ai.essayscoring.dto.ScoringResultDTO;
import com.study.ai.essayscoring.entity.Essay;
import com.study.ai.essayscoring.entity.Feedback;
import com.study.ai.essayscoring.entity.Score;
import com.study.ai.essayscoring.repository.EssayRepository;
import com.study.ai.essayscoring.repository.FeedbackRepository;
import com.study.ai.essayscoring.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 作文服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EssayService {
    
    private final EssayRepository essayRepository;
    private final FeedbackRepository feedbackRepository;
    private final ScoreRepository scoreRepository;
    private final AIScoringService aiScoringService;
    
    /**
     * 提交作文并进行AI评分
     */
    @Transactional
    public ScoringResultDTO submitAndScoreEssay(EssaySubmitDTO dto) {
        log.info("学生提交作文: {}", dto.getTitle());
        
        // 创建作文实体
        Essay essay = new Essay();
        essay.setStudentName(dto.getStudentName());
        essay.setStudentId(dto.getStudentId());
        essay.setTitle(dto.getTitle());
        essay.setContent(dto.getContent());
        essay.setGrade(dto.getGrade());
        essay.setEssayType(dto.getEssayType());
        
        // 保存作文
        Essay savedEssay = essayRepository.save(essay);

        // AI评分 — 同时生成评分和反馈（合并为一次API调用）
        AIScoringService.ScoringResult scoringResult = aiScoringService.scoreEssay(savedEssay);
        Score score = scoringResult.getScore();
        score.setEssay(savedEssay);
        scoreRepository.save(score);
        savedEssay.setScore(score);

        // 获取 AI 同时生成的反馈
        List<Feedback> feedbacks = scoringResult.getFeedbacks();
        feedbacks.forEach(f -> f.setEssay(savedEssay));
        feedbackRepository.saveAll(feedbacks);
        
        // 构建返回结果
        return buildScoringResultDTO(savedEssay, score, feedbacks);
    }
    
    /**
     * 根据ID查询评分结果
     */
    @Transactional(readOnly = true)
    public ScoringResultDTO getScoringResult(Long essayId) {
        Essay essay = essayRepository.findByIdWithScore(essayId)
                .orElseThrow(() -> new RuntimeException("作文不存在"));

        List<Feedback> feedbacks = feedbackRepository.findByEssayIdOrderBySeverityDesc(essayId);

        return buildScoringResultDTO(essay, essay.getScore(), feedbacks);
    }
    
    /**
     * 查询学生的所有作文
     */
    public List<Essay> getStudentEssays(String studentId) {
        return essayRepository.findByStudentIdOrderBySubmitTimeDesc(studentId);
    }
    
    /**
     * 查询所有作文（教师端）
     */
    public List<Essay> getAllEssays() {
        return essayRepository.findAll();
    }

    /**
     * 删除作文及其关联的评分和反馈
     */
    @Transactional
    public void deleteEssay(Long essayId) {
        Essay essay = essayRepository.findById(essayId)
                .orElseThrow(() -> new RuntimeException("作文不存在"));
        feedbackRepository.deleteByEssayId(essayId);
        scoreRepository.findByEssayId(essayId).ifPresent(scoreRepository::delete);
        essayRepository.delete(essay);
        log.info("作文已删除: id={}", essayId);
    }

    /**
     * 教师手动更新评分（覆盖 AI 评分）
     */
    @Transactional
    public ScoringResultDTO updateScore(Long essayId, ScoreUpdateRequest request) {
        Essay essay = essayRepository.findByIdWithScore(essayId)
                .orElseThrow(() -> new RuntimeException("作文不存在"));

        Score score = essay.getScore();
        if (score == null) {
            score = new Score();
            score.setEssay(essay);
            score.setModelVersion("manual");
        }

        if (request.getTotalScore() != null) score.setTotalScore(request.getTotalScore());
        if (request.getContentScore() != null) score.setContentScore(request.getContentScore());
        if (request.getStructureScore() != null) score.setStructureScore(request.getStructureScore());
        if (request.getLanguageScore() != null) score.setLanguageScore(request.getLanguageScore());
        if (request.getCreativityScore() != null) score.setCreativityScore(request.getCreativityScore());
        if (request.getContentComment() != null) score.setContentComment(request.getContentComment());
        if (request.getStructureComment() != null) score.setStructureComment(request.getStructureComment());
        if (request.getLanguageComment() != null) score.setLanguageComment(request.getLanguageComment());
        if (request.getCreativityComment() != null) score.setCreativityComment(request.getCreativityComment());
        if (request.getOverallComment() != null) score.setOverallComment(request.getOverallComment());

        score.setScoreTime(LocalDateTime.now());
        score.setModelVersion("manual");
        scoreRepository.save(score);

        List<Feedback> feedbacks = feedbackRepository.findByEssayIdOrderBySeverityDesc(essayId);
        log.info("教师手动更新评分: essayId={}", essayId);
        return buildScoringResultDTO(essay, score, feedbacks);
    }
    
    /**
     * 构建评分结果DTO
     */
    private ScoringResultDTO buildScoringResultDTO(Essay essay, Score score, List<Feedback> feedbacks) {
        ScoringResultDTO dto = new ScoringResultDTO();
        dto.setEssayId(essay.getId());
        dto.setTitle(essay.getTitle());
        
        if (score != null) {
            dto.setTotalScore(score.getTotalScore());
            dto.setContentScore(score.getContentScore());
            dto.setStructureScore(score.getStructureScore());
            dto.setLanguageScore(score.getLanguageScore());
            dto.setCreativityScore(score.getCreativityScore());
            dto.setContentComment(score.getContentComment());
            dto.setStructureComment(score.getStructureComment());
            dto.setLanguageComment(score.getLanguageComment());
            dto.setCreativityComment(score.getCreativityComment());
            dto.setOverallComment(score.getOverallComment());
        }
        
        if (feedbacks != null) {
            dto.setFeedbacks(feedbacks.stream()
                    .map(f -> new ScoringResultDTO.FeedbackDTO(
                            f.getFeedbackType(),
                            f.getPosition(),
                            f.getIssue(),
                            f.getSuggestion(),
                            f.getImprovedExample(),
                            f.getSeverity()
                    ))
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
}
