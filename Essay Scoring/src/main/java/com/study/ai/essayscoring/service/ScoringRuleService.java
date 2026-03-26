package com.study.ai.essayscoring.service;

import com.study.ai.essayscoring.entity.ScoringRule;
import com.study.ai.essayscoring.repository.ScoringRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评分规则服务（教师端）
 */
@Service
@RequiredArgsConstructor
public class ScoringRuleService {
    
    private final ScoringRuleRepository scoringRuleRepository;
    
    /**
     * 获取所有评分规则
     */
    public List<ScoringRule> getAllRules() {
        return scoringRuleRepository.findAll();
    }
    
    /**
     * 获取启用的评分规则
     */
    public List<ScoringRule> getEnabledRules() {
        return scoringRuleRepository.findByEnabledTrue();
    }
    
    /**
     * 根据年级和文体获取评分规则
     */
    public List<ScoringRule> getRulesByGradeAndType(String grade, String essayType) {
        return scoringRuleRepository.findByApplicableGradeAndApplicableEssayTypeAndEnabledTrue(grade, essayType);
    }
    
    /**
     * 创建评分规则
     */
    public ScoringRule createRule(ScoringRule rule) {
        return scoringRuleRepository.save(rule);
    }
    
    /**
     * 更新评分规则
     */
    public ScoringRule updateRule(Long id, ScoringRule rule) {
        ScoringRule existing = scoringRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("评分规则不存在"));
        
        existing.setRuleName(rule.getRuleName());
        existing.setApplicableGrade(rule.getApplicableGrade());
        existing.setApplicableEssayType(rule.getApplicableEssayType());
        existing.setDimension(rule.getDimension());
        existing.setWeight(rule.getWeight());
        existing.setCriteria(rule.getCriteria());
        existing.setEnabled(rule.getEnabled());
        
        return scoringRuleRepository.save(existing);
    }
    
    /**
     * 删除评分规则
     */
    public void deleteRule(Long id) {
        scoringRuleRepository.deleteById(id);
    }
    
    /**
     * 启用/禁用评分规则
     */
    public ScoringRule toggleRule(Long id) {
        ScoringRule rule = scoringRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("评分规则不存在"));
        rule.setEnabled(!rule.getEnabled());
        return scoringRuleRepository.save(rule);
    }
}
