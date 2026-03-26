package com.study.ai.essayscoring.service;

import com.study.ai.essayscoring.dto.EssaySubmitDTO;
import com.study.ai.essayscoring.dto.ScoringResultDTO;
import com.study.ai.essayscoring.entity.Essay;
import com.study.ai.essayscoring.entity.Feedback;
import com.study.ai.essayscoring.entity.Score;
import com.study.ai.essayscoring.repository.EssayRepository;
import com.study.ai.essayscoring.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        
        // AI评分
        Score score = aiScoringService.scoreEssay(savedEssay);
        savedEssay.setScore(score);
        essayRepository.save(savedEssay);
        
        // 生成反馈
        List<Feedback> feedbacks = aiScoringService.generateFeedbacks(savedEssay, score);
        feedbacks.forEach(f -> f.setEssay(savedEssay));
        feedbackRepository.saveAll(feedbacks);
        
        // 构建返回结果
        return buildScoringResultDTO(savedEssay, score, feedbacks);
    }
    
    /**
     * 根据ID查询评分结果
     */
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
