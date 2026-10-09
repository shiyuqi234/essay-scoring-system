package com.study.ai.essayscoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 评分规则创建/更新请求 —— 只暴露允许客户端控制的字段，
 * 不暴露 id / createTime / updateTime / createdBy 等由服务端管理的字段（防过度绑定）。
 */
@Data
public class ScoringRuleRequest {

    @NotBlank
    private String ruleName;

    @NotBlank
    private String dimension;

    @NotNull
    @Positive
    private Double weight;

    private String criteria;

    private String applicableGrade;

    private String applicableEssayType;

    private Boolean enabled;
}
