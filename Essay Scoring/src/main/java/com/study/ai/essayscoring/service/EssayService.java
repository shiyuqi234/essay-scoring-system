package com.study.ai.essayscoring.service;

import com.study.ai.essayscoring.dto.EssaySubmitDTO;
import com.study.ai.essayscoring.dto.EssaySummaryDTO;
import com.study.ai.essayscoring.dto.ScoreUpdateRequest;
import com.study.ai.essayscoring.dto.ScoringResultDTO;
import com.study.ai.essayscoring.entity.Essay;
import com.study.ai.essayscoring.entity.Feedback;
import com.study.ai.essayscoring.entity.Score;
import com.study.ai.essayscoring.exception.ResourceNotFoundException;
import com.study.ai.essayscoring.repository.EssayRepository;
import com.study.ai.essayscoring.repository.FeedbackRepository;
import com.study.ai.essayscoring.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
    private final TransactionTemplate txTemplate;

    /**
     * 提交作文并进行 AI 评分。
     *
     * 事务边界刻意拆成三段：
     *   ① 短事务：只保存作文；
     *   ② 事务外：调用 LLM（最长 60+ 秒，此期间不持有任何数据库连接）；
     *   ③ 短事务：只保存评分与反馈。
     *
     * 之所以不用「同类内拆两个 @Transactional 方法」：Spring 事务基于 AOP 代理，
     * 同类自调用不走代理，注解会静默失效，因此用 TransactionTemplate 显式划定边界。
     *
     * @param studentId 由服务端从 JWT/Principal 注入，绝不信任客户端请求体。
     */
    public ScoringResultDTO submitAndScoreEssay(EssaySubmitDTO dto, String studentId) {
        log.info("学生提交作文: {}", dto.getTitle());

        // ① 短事务：只保存作文
        Essay savedEssay = txTemplate.execute(status -> {
            Essay essay = new Essay();
            essay.setStudentName(dto.getStudentName());
            essay.setStudentId(studentId);          // ★ 服务端注入，不读 dto
            essay.setTitle(dto.getTitle());
            essay.setContent(dto.getContent());
            essay.setGrade(dto.getGrade());
            essay.setEssayType(dto.getEssayType());
            return essayRepository.save(essay);
        });
        Objects.requireNonNull(savedEssay, "保存作文失败");

        // ② 事务外：调用 LLM —— 这一步绝不持有数据库连接
        long t0 = System.currentTimeMillis();
        AIScoringService.ScoringResult scoringResult = aiScoringService.scoreEssay(savedEssay);
        log.info("LLM 调用耗时 {} ms（此期间未持有数据库连接）", System.currentTimeMillis() - t0);

        // ③ 短事务：只写库
        return txTemplate.execute(status -> {
            Score score = scoringResult.getScore();
            score.setEssay(savedEssay);
            scoreRepository.save(score);
            savedEssay.setScore(score);

            List<Feedback> feedbacks = scoringResult.getFeedbacks();
            feedbacks.forEach(f -> f.setEssay(savedEssay));
            feedbackRepository.saveAll(feedbacks);

            return buildScoringResultDTO(savedEssay, score, feedbacks);
        });
    }

    /**
     * 根据 ID 查询评分结果 —— 只在作文属于该学生（或调用者是教师，currentStudentId 为 null）时返回。
     * 归属不匹配时抛 ResourceNotFoundException（404），与「不存在」不可区分。
     */
    @Transactional(readOnly = true)
    public ScoringResultDTO getScoringResult(Long essayId, String currentStudentId) {
        Essay essay = essayRepository.findByIdWithScore(essayId)
                .orElseThrow(() -> new ResourceNotFoundException("作文不存在"));

        // ★ 归属校验：非本人则拒绝（放 404 而不是 403，避免暴露「该 id 存在」）
        if (currentStudentId != null && !currentStudentId.equals(essay.getStudentId())) {
            throw new ResourceNotFoundException("作文不存在");
        }

        List<Feedback> feedbacks = feedbackRepository.findByEssayIdOrderBySeverityDesc(essayId);

        return buildScoringResultDTO(essay, essay.getScore(), feedbacks);
    }

    /**
     * 查询学生的所有作文（返回摘要 DTO，不含正文/反馈，避免懒加载；studentId 由 Controller 从 Principal 解析后传入）
     */
    public List<EssaySummaryDTO> getStudentEssays(String studentId) {
        return essayRepository.findSummariesByStudentId(studentId);
    }

    /**
     * 查询所有作文摘要（教师端）
     */
    public List<EssaySummaryDTO> getAllEssays() {
        return essayRepository.findAllSummaries();
    }

    /**
     * 统计信息（数据库聚合，避免全表扫描 + 内存遍历）。
     * 返回 [总作文数, 学生数, 平均分]。
     */
    public Object[] getStatistics() {
        return essayRepository.aggregateStatistics();
    }

    /**
     * 删除作文及其关联的评分和反馈
     */
    @Transactional
    public void deleteEssay(Long essayId) {
        Essay essay = essayRepository.findById(essayId)
                .orElseThrow(() -> new ResourceNotFoundException("作文不存在"));
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
                .orElseThrow(() -> new ResourceNotFoundException("作文不存在"));

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
