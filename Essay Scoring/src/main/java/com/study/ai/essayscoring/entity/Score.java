package com.study.ai.essayscoring.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评分结果实体类
 */
@Entity
@Table(name = "scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Score {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联的作文
     */
    @OneToOne
    @JoinColumn(name = "essay_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"score", "feedbacks"})  // 避免无限递归
    private Essay essay;
    
    /**
     * 总分（满分100）
     */
    @Column(nullable = false)
    private Double totalScore;
    
    /**
     * 内容得分（满分30）
     */
    @Column(nullable = false)
    private Double contentScore;
    
    /**
     * 结构得分（满分25）
     */
    @Column(nullable = false)
    private Double structureScore;
    
    /**
     * 语言表达得分（满分25）
     */
    @Column(nullable = false)
    private Double languageScore;
    
    /**
     * 创意得分（满分20）
     */
    @Column(nullable = false)
    private Double creativityScore;
    
    /**
     * 内容评价详情
     */
    @Column(columnDefinition = "TEXT")
    private String contentComment;
    
    /**
     * 结构评价详情
     */
    @Column(columnDefinition = "TEXT")
    private String structureComment;
    
    /**
     * 语言评价详情
     */
    @Column(columnDefinition = "TEXT")
    private String languageComment;
    
    /**
     * 创意评价详情
     */
    @Column(columnDefinition = "TEXT")
    private String creativityComment;
    
    /**
     * 综合评语
     */
    @Column(columnDefinition = "TEXT")
    private String overallComment;
    
    /**
     * 评分时间
     */
    @Column(nullable = false)
    private LocalDateTime scoreTime;
    
    /**
     * 评分模型版本
     */
    private String modelVersion;
    
    @PrePersist
    protected void onCreate() {
        if (scoreTime == null) {
            scoreTime = LocalDateTime.now();
        }
    }
}
