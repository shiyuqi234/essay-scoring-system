package com.study.ai.essayscoring.dto;

import lombok.Data;

/**
 * 教师手动修改评分请求
 */
@Data
public class ScoreUpdateRequest {
    private Double totalScore;
    private Double contentScore;
    private Double structureScore;
    private Double languageScore;
    private Double creativityScore;
    private String contentComment;
    private String structureComment;
    private String languageComment;
    private String creativityComment;
    private String overallComment;
}
