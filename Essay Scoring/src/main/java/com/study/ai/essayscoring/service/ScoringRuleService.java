package com.study.ai.essayscoring.service;

import com.study.ai.essayscoring.dto.ScoringRuleRequest;
import com.study.ai.essayscoring.entity.ScoringRule;
import com.study.ai.essayscoring.exception.ResourceNotFoundException;
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
     * 创建评分规则（收 DTO，由 Service 转实体，防过度绑定）
     */
    public ScoringRule createRule(ScoringRuleRequest req) {
        ScoringRule rule = new ScoringRule();
        applyRequest(rule, req);
        if (rule.getEnabled() == null) {
            rule.setEnabled(true);
        }
        return scoringRuleRepository.save(rule);
    }

    /**
     * 更新评分规则（收 DTO，由 Service 转实体）
     */
    public ScoringRule updateRule(Long id, ScoringRuleRequest req) {
        ScoringRule existing = scoringRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("评分规则不存在"));
        applyRequest(existing, req);
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
                .orElseThrow(() -> new ResourceNotFoundException("评分规则不存在"));
        rule.setEnabled(!rule.getEnabled());
        return scoringRuleRepository.save(rule);
    }

    /**
     * 把 DTO 的字段写到实体上。enabled 为 null 时保持原值（更新场景）或默认值（创建场景）。
     */
    private void applyRequest(ScoringRule rule, ScoringRuleRequest req) {
        rule.setRuleName(req.getRuleName());
        rule.setApplicableGrade(req.getApplicableGrade());
        rule.setApplicableEssayType(req.getApplicableEssayType());
        rule.setDimension(req.getDimension());
        rule.setWeight(req.getWeight());
        rule.setCriteria(req.getCriteria());
        if (req.getEnabled() != null) {
            rule.setEnabled(req.getEnabled());
        }
    }
}
