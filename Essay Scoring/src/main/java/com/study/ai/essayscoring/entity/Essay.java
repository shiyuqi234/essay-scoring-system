package com.study.ai.essayscoring.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作文实体类
 */
@Entity
@Table(name = "essays")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Essay {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 学生姓名
     */
    @Column(nullable = false)
    private String studentName;
    
    /**
     * 学生ID
     */
    @Column(nullable = false)
    private String studentId;
    
    /**
     * 作文标题
     */
    @Column(nullable = false)
    private String title;
    
    /**
     * 作文内容
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    /**
     * 年级（如：小学三年级、初中二年级、高中一年级）
     */
    @Column(nullable = false)
    private String grade;
    
    /**
     * 文体类型（记叙文、议论文、说明文、应用文等）
     */
    @Column(nullable = false)
    private String essayType;
    
    /**
     * 字数
     */
    private Integer wordCount;
    
    /**
     * 提交时间
     */
    @Column(nullable = false)
    private LocalDateTime submitTime;
    
    /**
     * 评分结果（一对一关系）
     */
    @OneToOne(mappedBy = "essay", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("essay")  // 避免无限递归
    private Score score;
    
    /**
     * 反馈列表（一对多关系）
     */
    @OneToMany(mappedBy = "essay", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("essay")  // 避免无限递归
    private List<Feedback> feedbacks;
    
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
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (submitTime == null) {
            submitTime = LocalDateTime.now();
        }
        if (wordCount == null && content != null) {
            wordCount = countCharacters(content);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
        if (wordCount == null && content != null) {
            wordCount = countCharacters(content);
        }
    }

    /**
     * 统计有效中文字数（排除空格、换行等空白字符）
     */
    private static int countCharacters(String text) {
        if (text == null) return 0;
        int count = 0;
        for (char c : text.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                count++;
            }
        }
        return count;
    }
}
