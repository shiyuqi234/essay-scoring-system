package com.study.ai.essayscoring.repository;

import com.study.ai.essayscoring.entity.ScoringRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评分规则Repository
 */
@Repository
public interface ScoringRuleRepository extends JpaRepository<ScoringRule, Long> {
    
    /**
     * 查询启用的评分规则
     */
    List<ScoringRule> findByEnabledTrue();
    
    /**
     * 根据年级和文体查询评分规则
     */
    List<ScoringRule> findByApplicableGradeAndApplicableEssayTypeAndEnabledTrue(
            String grade, String essayType);
    
    /**
     * 根据维度查询评分规则
     */
    List<ScoringRule> findByDimensionAndEnabledTrue(String dimension);
}
