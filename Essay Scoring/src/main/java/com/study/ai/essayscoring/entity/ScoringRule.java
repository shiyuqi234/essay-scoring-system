package com.study.ai.essayscoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评分规则实体类（教师端管理）
 */
@Entity
@Table(name = "scoring_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoringRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 规则名称
     */
    @Column(nullable = false)
    private String ruleName;
    
    /**
     * 适用年级
     */
    private String applicableGrade;
    
    /**
     * 适用文体
     */
    private String applicableEssayType;
    
    /**
     * 评分维度（content、structure、language、creativity）
     */
    @Column(nullable = false)
    private String dimension;
    
    /**
     * 权重（0-1之间的小数）
     */
    @Column(nullable = false)
    private Double weight;
    
    /**
     * 评分标准描述
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String criteria;
    
    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = true;
    
    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updateTime;
    
    /**
     * 创建人（教师ID）
     */
    private String createdBy;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
