package com.study.ai.essayscoring.controller;

import com.study.ai.essayscoring.dto.ApiResponse;
import com.study.ai.essayscoring.dto.EssaySubmitDTO;
import com.study.ai.essayscoring.dto.EssaySummaryDTO;
import com.study.ai.essayscoring.dto.ScoringResultDTO;
import com.study.ai.essayscoring.entity.EssayStatus;
import com.study.ai.essayscoring.entity.UserAccount;
import com.study.ai.essayscoring.exception.ResourceNotFoundException;
import com.study.ai.essayscoring.repository.UserAccountRepository;
import com.study.ai.essayscoring.service.EssayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生端API控制器
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:8080}")
public class StudentController {

    private final EssayService essayService;
    private final UserAccountRepository userAccountRepository;

    /**
     * 从 JWT 解析出的当前登录用户，拿他真实的 studentId（不信任请求参数）。
     */
    private String currentStudentId(Principal principal) {
        if (principal == null) {
            throw new ResourceNotFoundException("未登录");
        }
        UserAccount account = userAccountRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return account.getStudentId();
    }

    /**
     * 提交作文并进行评分
     */
    @PostMapping("/essay/submit")
    public ResponseEntity<ApiResponse> submitEssay(@Valid @RequestBody EssaySubmitDTO dto,
                                                   Principal principal) {
        // ★ 用服务端的 studentId 覆盖，杜绝冒用他人学号提交
        ScoringResultDTO result = essayService.submitAndScoreEssay(dto, currentStudentId(principal));
        return ResponseEntity.ok(ApiResponse.success("作文提交成功，评分完成", result));
    }

    /**
     * 异步提交作文：立即返回 essayId，后台异步评分（PENDING → SCORING → SUCCESS/FAILED）
     */
    @PostMapping("/essay/submit-async")
    public ResponseEntity<ApiResponse> submitEssayAsync(@Valid @RequestBody EssaySubmitDTO dto,
                                                        Principal principal) {
        Long essayId = essayService.submitEssayAsync(dto, currentStudentId(principal));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("essayId", essayId);
        data.put("status", EssayStatus.PENDING.name());
        return ResponseEntity.ok(ApiResponse.success("作文已提交，正在后台评分", data));
    }

    /**
     * 查询评分结果
     */
    @GetMapping("/essay/{id}/result")
    public ResponseEntity<ApiResponse> getScoringResult(@PathVariable Long id,
                                                        Principal principal) {
        ScoringResultDTO result = essayService.getScoringResult(id, currentStudentId(principal));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 查询作文评分状态（异步评分状态机）
     */
    @GetMapping("/essay/{id}/status")
    public ResponseEntity<ApiResponse> getEssayStatus(@PathVariable Long id,
                                                      Principal principal) {
        EssayStatus status = essayService.getEssayStatus(id, currentStudentId(principal));
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    /**
     * 查询学生的所有作文
     */
    @GetMapping("/essays")
    public ResponseEntity<ApiResponse> getStudentEssays(Principal principal) {
        // ★ 去掉了 @RequestParam studentId —— 参数不存在就无法被篡改
        List<EssaySummaryDTO> essays = essayService.getStudentEssays(currentStudentId(principal));
        return ResponseEntity.ok(ApiResponse.successWithCount(essays, essays.size()));
    }
}
