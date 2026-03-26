package com.study.ai.essayscoring.controller;

import com.study.ai.essayscoring.entity.Essay;
import com.study.ai.essayscoring.entity.ScoringRule;
import com.study.ai.essayscoring.service.EssayService;
import com.study.ai.essayscoring.service.ScoringRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教师端API控制器
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TeacherController {
    
    private final EssayService essayService;
    private final ScoringRuleService scoringRuleService;
    
    /**
     * 获取所有作文列表
     */
    @GetMapping("/essays")
    public ResponseEntity<Map<String, Object>> getAllEssays() {
        try {
            List<Essay> essays = essayService.getAllEssays();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", essays);
            response.put("count", essays.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取所有评分规则
     */
    @GetMapping("/rules")
    public ResponseEntity<Map<String, Object>> getAllRules() {
        try {
            List<ScoringRule> rules = scoringRuleService.getAllRules();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", rules);
            response.put("count", rules.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 创建评分规则
     */
    @PostMapping("/rules")
    public ResponseEntity<Map<String, Object>> createRule(@RequestBody ScoringRule rule) {
        try {
            ScoringRule created = scoringRuleService.createRule(rule);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "评分规则创建成功");
            response.put("data", created);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新评分规则
     */
    @PutMapping("/rules/{id}")
    public ResponseEntity<Map<String, Object>> updateRule(
            @PathVariable Long id, 
            @RequestBody ScoringRule rule) {
        try {
            ScoringRule updated = scoringRuleService.updateRule(id, rule);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "评分规则更新成功");
            response.put("data", updated);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除评分规则
     */
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Map<String, Object>> deleteRule(@PathVariable Long id) {
        try {
            scoringRuleService.deleteRule(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "评分规则删除成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 启用/禁用评分规则
     */
    @PutMapping("/rules/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleRule(@PathVariable Long id) {
        try {
            ScoringRule rule = scoringRuleService.toggleRule(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", rule.getEnabled() ? "规则已启用" : "规则已禁用");
            response.put("data", rule);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            List<Essay> essays = essayService.getAllEssays();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalEssays", essays.size());
            stats.put("totalStudents", essays.stream()
                    .map(Essay::getStudentId)
                    .distinct()
                    .count());
            
            // 计算平均分
            double avgScore = essays.stream()
                    .filter(e -> e.getScore() != null && e.getScore().getTotalScore() != null)
                    .mapToDouble(e -> e.getScore().getTotalScore())
                    .average()
                    .orElse(0.0);
            stats.put("averageScore", avgScore);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
