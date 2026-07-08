package com.study.ai.essayscoring.controller;

import com.study.ai.essayscoring.dto.ApiResponse;
import com.study.ai.essayscoring.dto.EssaySubmitDTO;
import com.study.ai.essayscoring.dto.ScoringResultDTO;
import com.study.ai.essayscoring.entity.Essay;
import com.study.ai.essayscoring.service.EssayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生端API控制器
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:8080}")
public class StudentController {

    private final EssayService essayService;

    /**
     * 提交作文并进行评分
     */
    @PostMapping("/essay/submit")
    public ResponseEntity<ApiResponse> submitEssay(@Valid @RequestBody EssaySubmitDTO dto) {
        ScoringResultDTO result = essayService.submitAndScoreEssay(dto);
        return ResponseEntity.ok(ApiResponse.success("作文提交成功，评分完成", result));
    }

    /**
     * 查询评分结果
     */
    @GetMapping("/essay/{id}/result")
    public ResponseEntity<ApiResponse> getScoringResult(@PathVariable Long id) {
        ScoringResultDTO result = essayService.getScoringResult(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 查询学生的所有作文
     */
    @GetMapping("/essays")
    public ResponseEntity<ApiResponse> getStudentEssays(@RequestParam String studentId) {
        List<Essay> essays = essayService.getStudentEssays(studentId);
        return ResponseEntity.ok(ApiResponse.successWithCount(essays, essays.size()));
    }
}
