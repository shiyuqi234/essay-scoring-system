package com.study.ai.essayscoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 评分结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoringResultDTO {
    
    private Long essayId;
    private String title;
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
    private List<FeedbackDTO> feedbacks;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedbackDTO {
        private String feedbackType;
        private String position;
        private String issue;
        private String suggestion;
        private String improvedExample;
        private String severity;
    }
}
