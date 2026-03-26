package com.study.ai.essayscoring.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 作文提交DTO
 */
@Data
public class EssaySubmitDTO {
    
    @NotBlank(message = "学生姓名不能为空")
    private String studentName;
    
    @NotBlank(message = "学生ID不能为空")
    private String studentId;
    
    @NotBlank(message = "作文标题不能为空")
    private String title;
    
    @NotBlank(message = "作文内容不能为空")
    private String content;
    
    @NotBlank(message = "年级不能为空")
    private String grade;
    
    @NotBlank(message = "文体类型不能为空")
    private String essayType;
}
