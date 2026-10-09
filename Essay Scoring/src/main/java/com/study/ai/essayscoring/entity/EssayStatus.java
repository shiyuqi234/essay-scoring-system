package com.study.ai.essayscoring.entity;

/**
 * 作文评分状态机：PENDING → SCORING → SUCCESS / FAILED。
 * 异步评分流程：提交时 PENDING，后台开始评分 SCORING，完成后 SUCCESS（或降级/异常 FAILED）。
 */
public enum EssayStatus {
    PENDING,
    SCORING,
    SUCCESS,
    FAILED
}
