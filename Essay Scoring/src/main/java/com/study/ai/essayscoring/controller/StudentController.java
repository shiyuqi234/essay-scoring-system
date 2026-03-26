package com.study.ai.essayscoring.controller;

import com.study.ai.essayscoring.dto.EssaySubmitDTO;
import com.study.ai.essayscoring.dto.ScoringResultDTO;
import com.study.ai.essayscoring.entity.Essay;
import com.study.ai.essayscoring.service.EssayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生端API控制器
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentController {
    
    private final EssayService essayService;
    
    /**
     * 提交作文并进行评分
     */
    @PostMapping("/essay/submit")
    public ResponseEntity<Map<String, Object>> submitEssay(@Valid @RequestBody EssaySubmitDTO dto) {
        try {
            ScoringResultDTO result = essayService.submitAndScoreEssay(dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "作文提交成功，评分完成");
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "提交失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 查询评分结果
     */
    @GetMapping("/essay/{id}/result")
    public ResponseEntity<Map<String, Object>> getScoringResult(@PathVariable Long id) {
        try {
            ScoringResultDTO result = essayService.getScoringResult(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 查询学生的所有作文
     */
    @GetMapping("/essays")
    public ResponseEntity<Map<String, Object>> getStudentEssays(
            @RequestParam String studentId) {
        try {
            List<Essay> essays = essayService.getStudentEssays(studentId);
            
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
}
