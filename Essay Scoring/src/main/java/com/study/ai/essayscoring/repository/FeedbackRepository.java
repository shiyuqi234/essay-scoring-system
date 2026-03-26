package com.study.ai.essayscoring.repository;

import com.study.ai.essayscoring.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 反馈Repository
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    /**
     * 根据作文ID查询反馈列表
     */
    List<Feedback> findByEssayIdOrderBySeverityDesc(Long essayId);
}
