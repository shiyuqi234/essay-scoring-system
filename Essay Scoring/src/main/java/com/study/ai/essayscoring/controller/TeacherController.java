package com.study.ai.essayscoring.controller;

import com.study.ai.essayscoring.dto.ApiResponse;
import com.study.ai.essayscoring.dto.ScoreUpdateRequest;
import com.study.ai.essayscoring.dto.ScoringResultDTO;
import com.study.ai.essayscoring.entity.Essay;
import com.study.ai.essayscoring.entity.ScoringRule;
import com.study.ai.essayscoring.service.EssayService;
import com.study.ai.essayscoring.service.ScoringRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 教师端API控制器
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:8080}")
public class TeacherController {

    private final EssayService essayService;
    private final ScoringRuleService scoringRuleService;

    /**
     * 获取所有作文列表
     */
    @GetMapping("/essays")
    public ResponseEntity<ApiResponse> getAllEssays() {
        List<Essay> essays = essayService.getAllEssays();
        return ResponseEntity.ok(ApiResponse.successWithCount(essays, essays.size()));
    }

    /**
     * 获取所有评分规则
     */
    @GetMapping("/rules")
    public ResponseEntity<ApiResponse> getAllRules() {
        List<ScoringRule> rules = scoringRuleService.getAllRules();
        return ResponseEntity.ok(ApiResponse.successWithCount(rules, rules.size()));
    }

    /**
     * 创建评分规则
     */
    @PostMapping("/rules")
    public ResponseEntity<ApiResponse> createRule(@RequestBody ScoringRule rule) {
        ScoringRule created = scoringRuleService.createRule(rule);
        return ResponseEntity.ok(ApiResponse.success("评分规则创建成功", created));
    }

    /**
     * 更新评分规则
     */
    @PutMapping("/rules/{id}")
    public ResponseEntity<ApiResponse> updateRule(@PathVariable Long id, @RequestBody ScoringRule rule) {
        ScoringRule updated = scoringRuleService.updateRule(id, rule);
        return ResponseEntity.ok(ApiResponse.success("评分规则更新成功", updated));
    }

    /**
     * 删除评分规则
     */
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<ApiResponse> deleteRule(@PathVariable Long id) {
        scoringRuleService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.success("评分规则删除成功", null));
    }

    /**
     * 启用/禁用评分规则
     */
    @PutMapping("/rules/{id}/toggle")
    public ResponseEntity<ApiResponse> toggleRule(@PathVariable Long id) {
        ScoringRule rule = scoringRuleService.toggleRule(id);
        String msg = rule.getEnabled() ? "规则已启用" : "规则已禁用";
        return ResponseEntity.ok(ApiResponse.success(msg, rule));
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse> getStatistics() {
        List<Essay> essays = essayService.getAllEssays();

        long totalEssays = essays.size();
        long totalStudents = essays.stream()
                .map(Essay::getStudentId)
                .distinct()
                .count();

        double avgScore = essays.stream()
                .filter(e -> e.getScore() != null && e.getScore().getTotalScore() != null)
                .mapToDouble(e -> e.getScore().getTotalScore())
                .average()
                .orElse(0.0);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalEssays", totalEssays);
        stats.put("totalStudents", totalStudents);
        stats.put("averageScore", Math.round(avgScore * 100.0) / 100.0);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 删除作文（及关联的评分和反馈）
     */
    @DeleteMapping("/essay/{id}")
    public ResponseEntity<ApiResponse> deleteEssay(@PathVariable Long id) {
        essayService.deleteEssay(id);
        return ResponseEntity.ok(ApiResponse.success("作文已删除", null));
    }

    /**
     * 教师手动更新评分
     */
    @PutMapping("/essay/{id}/score")
    public ResponseEntity<ApiResponse> updateScore(@PathVariable Long id,
                                                    @RequestBody ScoreUpdateRequest request) {
        ScoringResultDTO result = essayService.updateScore(id, request);
        return ResponseEntity.ok(ApiResponse.success("评分已更新", result));
    }
}
