package com.study.ai.essayscoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 作文列表摘要 DTO —— 列表接口专用。
 * 不含作文正文 content 与反馈列表，避免序列化时触发懒加载（关 OSIV 后尤其关键）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EssaySummaryDTO {

    private Long id;
    private String studentName;
    private String studentId;
    private String title;
    private String grade;
    private String essayType;
    private Integer wordCount;
    private LocalDateTime submitTime;
    private Double totalScore;
}
