package com.study.ai.essayscoring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.ai.essayscoring.entity.Essay;
import com.study.ai.essayscoring.exception.TruncatedResponseException;
import com.study.ai.essayscoring.repository.ScoringRuleRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.cache.CacheManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * AI 评分服务评测集测试 —— 数据驱动，离线可运行。
 *
 * 通过 Mockito 打桩 ChatClient（LLM 调用），用固定样本覆盖：
 *   正常评分 / 分数越界裁剪 / 字段缺失默认值 / feedbacks 校验 /
 *   JSON 提取 / 异常降级 / 截断重试 等分支，
 * 使含 LLM 调用的评分逻辑可在 CI 中稳定回归，无需真实 API Key。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AIScoringServiceEvalTest {

    @Mock
    ChatClient chatClient;

    @Mock
    ScoringRuleRepository scoringRuleRepository;

    @Mock
    CacheManager cacheManager;

    // ── 评测集加载 ────────────────────────────────────────────

    static Stream<CaseSpec> evalCases() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = AIScoringServiceEvalTest.class.getClassLoader()
                .getResourceAsStream("ai-scoring-eval.json")) {
            JsonNode root = mapper.readTree(in);
            List<CaseSpec> cases = new ArrayList<>();
            for (JsonNode node : root.get("cases")) {
                cases.add(mapper.treeToValue(node, CaseSpec.class));
            }
            return cases.stream();
        }
    }

    // ── 评测主循环 ────────────────────────────────────────────

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("evalCases")
    void evaluate(CaseSpec spec) {
        AIScoringService service = new AIScoringService(chatClient, scoringRuleRepository, cacheManager);

        when(scoringRuleRepository
                .findByApplicableGradeAndApplicableEssayTypeAndEnabledTrue(any(), any()))
                .thenReturn(List.of());

        Essay essay = new Essay();
        essay.setId(1L);
        essay.setTitle("测试作文");
        essay.setGrade("高中");
        essay.setEssayType("议论文");
        essay.setContent("这是一篇用于评测的作文内容，围绕主题展开，结构清晰，语言通顺。");

        // 按样本打桩 ChatClient
        MockCase mc = spec.mock;
        if (mc.exception != null) {
            if ("TruncatedResponseException".equals(mc.exception)) {
                if (mc.responses == null || mc.responses.isEmpty()) {
                    when(chatClient.call(any()))
                            .thenThrow(new TruncatedResponseException("输出被截断"));
                } else {
                    when(chatClient.call(any()))
                            .thenThrow(new TruncatedResponseException("输出被截断"))
                            .thenReturn(mc.responses.get(0));
                }
            } else {
                when(chatClient.call(any())).thenThrow(new RuntimeException("LLM 调用失败"));
            }
        } else {
            OngoingStubbing<String> stubbing = when(chatClient.call(any()));
            for (String r : mc.responses) {
                stubbing = stubbing.thenReturn(r);
            }
        }

        var result = service.scoreEssay(essay);
        ExpectSpec exp = spec.expect;

        assertNotNull(result, spec.id + ": result 不应为 null");
        assertEquals(exp.success, result.isSuccess(), spec.id + ": isSuccess");
        if (exp.success) {
            assertNull(result.getError(), spec.id + ": 成功时 error 应为 null");
        } else {
            assertNotNull(result.getError(), spec.id + ": 失败时 error 不应为 null");
        }

        if (exp.totalScore != null) {
            assertEquals(exp.totalScore, result.getScore().getTotalScore(), spec.id + ": totalScore");
        }
        if (exp.contentScore != null) {
            assertEquals(exp.contentScore, result.getScore().getContentScore(), spec.id + ": contentScore");
        }
        if (exp.structureScore != null) {
            assertEquals(exp.structureScore, result.getScore().getStructureScore(), spec.id + ": structureScore");
        }
        if (exp.languageScore != null) {
            assertEquals(exp.languageScore, result.getScore().getLanguageScore(), spec.id + ": languageScore");
        }
        if (exp.creativityScore != null) {
            assertEquals(exp.creativityScore, result.getScore().getCreativityScore(), spec.id + ": creativityScore");
        }
        if (exp.feedbackCount != null) {
            assertEquals(exp.feedbackCount, result.getFeedbacks().size(), spec.id + ": feedbackCount");
        }
        if (exp.hasPraise != null) {
            boolean actualPraise = result.getFeedbacks().stream()
                    .anyMatch(f -> "亮点表扬".equals(f.getFeedbackType()));
            assertEquals(exp.hasPraise, actualPraise, spec.id + ": hasPraise");
        }
        if (exp.errorContains != null) {
            assertTrue(result.getError() != null && result.getError().contains(exp.errorContains),
                    spec.id + ": error 应包含 " + exp.errorContains + "，实际=" + result.getError());
        }
    }

    // ── 评测集结构（Jackson 反序列化）─────────────────────────

    static class CaseSpec {
        public String id;
        public String desc;
        public MockCase mock;
        public ExpectSpec expect;

        @Override
        public String toString() {
            return id + " - " + desc;
        }
    }

    static class MockCase {
        public List<String> responses;
        public String exception;
    }

    static class ExpectSpec {
        public boolean success;
        public Double totalScore;
        public Double contentScore;
        public Double structureScore;
        public Double languageScore;
        public Double creativityScore;
        public Integer feedbackCount;
        public Boolean hasPraise;
        public String errorContains;
    }
}
