package com.study.ai.essayscoring.controller;

import com.study.ai.essayscoring.dto.ApiResponse;
import com.study.ai.essayscoring.dto.ScoreUpdateRequest;
import com.study.ai.essayscoring.dto.ScoringResultDTO;
import com.study.ai.essayscoring.dto.ScoringRuleRequest;
import com.study.ai.essayscoring.dto.EssaySummaryDTO;
import com.study.ai.essayscoring.entity.ScoringRule;
import com.study.ai.essayscoring.service.EssayService;
import com.study.ai.essayscoring.service.ScoringRuleService;
import jakarta.validation.Valid;
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
        List<EssaySummaryDTO> essays = essayService.getAllEssays();
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
     * 创建评分规则（收 DTO，不直接收实体，防过度绑定）
     */
    @PostMapping("/rules")
    public ResponseEntity<ApiResponse> createRule(@Valid @RequestBody ScoringRuleRequest req) {
        ScoringRule created = scoringRuleService.createRule(req);
        return ResponseEntity.ok(ApiResponse.success("评分规则创建成功", created));
    }

    /**
     * 更新评分规则（收 DTO）
     */
    @PutMapping("/rules/{id}")
    public ResponseEntity<ApiResponse> updateRule(@PathVariable Long id,
                                                  @Valid @RequestBody ScoringRuleRequest req) {
        ScoringRule updated = scoringRuleService.updateRule(id, req);
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
     * 获取统计信息 —— 下推到数据库聚合，避免全表扫描 + 内存遍历。
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse> getStatistics() {
        Object[] row = essayService.getStatistics();

        long totalEssays = row[0] != null ? ((Number) row[0]).longValue() : 0L;
        long totalStudents = row[1] != null ? ((Number) row[1]).longValue() : 0L;
        double avgScore = row[2] != null
                ? Math.round(((Number) row[2]).doubleValue() * 100.0) / 100.0
                : 0.0;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalEssays", totalEssays);
        stats.put("totalStudents", totalStudents);
        stats.put("averageScore", avgScore);

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
