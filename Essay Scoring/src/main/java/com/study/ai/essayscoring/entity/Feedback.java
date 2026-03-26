package com.study.ai.essayscoring.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 反馈建议实体类
 */
@Entity
@Table(name = "feedbacks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联的作文
     */
    @ManyToOne
    @JoinColumn(name = "essay_id", nullable = false)
    @JsonIgnoreProperties({"score", "feedbacks"})  // 避免无限递归
    private Essay essay;
    
    /**
     * 反馈类型（语法错误、表达优化、结构建议、词汇建议等）
     */
    @Column(nullable = false)
    private String feedbackType;
    
    /**
     * 问题位置（原文中的位置，如行号或字符位置）
     */
    private String position;
    
    /**
     * 问题描述
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String issue;
    
    /**
     * 改进建议
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String suggestion;
    
    /**
     * 优化示例（改进后的句子或段落）
     */
    @Column(columnDefinition = "TEXT")
    private String improvedExample;
    
    /**
     * 严重程度（高、中、低）
     */
    @Column(nullable = false)
    private String severity;
    
    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
