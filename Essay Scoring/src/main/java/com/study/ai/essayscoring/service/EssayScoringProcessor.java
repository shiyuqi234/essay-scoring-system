package com.study.ai.essayscoring.service;

import com.study.ai.essayscoring.entity.Essay;
import com.study.ai.essayscoring.entity.EssayStatus;
import com.study.ai.essayscoring.entity.Feedback;
import com.study.ai.essayscoring.entity.Score;
import com.study.ai.essayscoring.repository.EssayRepository;
import com.study.ai.essayscoring.repository.FeedbackRepository;
import com.study.ai.essayscoring.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * 异步评分处理器 —— 独立 Bean 承载 @Async 评分，避免同类自调用导致 @Async 失效。
 *
 * 状态机：PENDING（提交时）→ SCORING（开始评分）→ SUCCESS / FAILED。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EssayScoringProcessor {

    private final EssayRepository essayRepository;
    private final ScoreRepository scoreRepository;
    private final FeedbackRepository feedbackRepository;
    private final AIScoringService aiScoringService;
    private final TransactionTemplate txTemplate;

    /**
     * 后台异步评分：SCORING → 调 LLM → 写库 → SUCCESS/FAILED。
     */
    @Async
    public void scoreAsync(Long essayId) {
        Essay essay = essayRepository.findById(essayId).orElse(null);
        if (essay == null) {
            log.warn("异步评分：作文不存在，essayId={}", essayId);
            return;
        }

        essay.setStatus(EssayStatus.SCORING);
        essayRepository.save(essay);

        try {
            AIScoringService.ScoringResult result = aiScoringService.scoreEssay(essay);

            txTemplate.execute(status -> {
                Score score = result.getScore();
                score.setEssay(essay);
                scoreRepository.save(score);
                essay.setScore(score);

                List<Feedback> feedbacks = result.getFeedbacks();
                feedbacks.forEach(f -> f.setEssay(essay));
                feedbackRepository.saveAll(feedbacks);
                return null;
            });

            essay.setStatus(EssayStatus.SUCCESS);
        } catch (Exception e) {
            log.error("异步评分失败，essayId={}", essayId, e);
            essay.setStatus(EssayStatus.FAILED);
        }
        essayRepository.save(essay);
        log.info("异步评分完成，essayId={}, status={}", essayId, essay.getStatus());
    }
}
