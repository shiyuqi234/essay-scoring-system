# 作文评分系统 — 实训演示改进文档

> 时间节点：周二（今天）→ 周五演示答辩 + 交报告  
> 原则：只做必须的，不折腾大的。
> **最后更新：2026-07-08**

---

## ✅ 已完成的安全修复（2026-07-08）

以下问题已在本次更新中修复：

| 修复项 | 状态 |
|--------|------|
| API Key 改用环境变量 `DEEPSEEK_API_KEY` | ✅ 已完成 |
| JWT Secret 改用环境变量 `JWT_SECRET` | ✅ 已完成 |
| CORS 由 `*` 改为可配置 `CORS_ORIGINS` | ✅ 已完成 |
| `.gitignore` 排除数据库文件（`*.mv.db`） | ✅ 已完成 |
| `TestController` 密码生成端点已禁用 | ✅ 已完成 |
| `AIConfig.java` 响应类添加 `@JsonIgnoreProperties` 修复 JSON 解析 Bug | ✅ 已完成 |
| MockChatClient 补充 feedbacks 模拟数据 | ✅ 已完成 |
| `AIScoringService` 线程安全问题（`lastFeedbacks` → `ScoringResult`） | ✅ 已完成 |
| `DeepSeekChatClient` 注入 Spring 管理的 ObjectMapper | ✅ 已完成 |
| `demos/` 目录已删除 | ✅ 已完成 |
| `GlobalExceptionHandler` 已实现 | ✅ 已完成 |
| 学生端 UI 重构（加载动画居中、空状态引导、综合评语展示） | ✅ 已完成 |

---

## 一、代码修复（今天 30 分钟搞定）

### 1.1 移除硬编码敏感信息

**文件**：`Essay Scoring/src/main/resources/application.yml`

当前问题：API Key 和数据库密码直接写死在配置文件中。

修改方式 — 用环境变量替代：

```yaml
# 修改前
datasource:
  password: 123456789

spring:
  cloud:
    ai:
      deepseek:
        api-key: sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# 修改后
datasource:
  password: ${DB_PASSWORD:123456}

spring:
  cloud:
    ai:
      deepseek:
        api-key: ${DEEPSEEK_API_KEY:your-api-key-here}
```

同时在项目根目录新建 `.env.example` 文件（不包含真实值）：

```
DB_PASSWORD=你的数据库密码
DEEPSEEK_API_KEY=你的 DeepSeek API Key
JWT_SECRET=你的JWT密钥
```

### 1.2 清理死代码

删除以下无用文件和目录：

```
Essay Scoring/src/main/java/com/study/ai/essayscoring/demos/    （整个目录删除）
sql/init.sql                                                     （空文件，删除或补内容）
Essay Scoring/src/main/resources/static/index.html               （只有 "hello word!!!" 占位，删除）
```

### 1.3 小改进（加分但不必须）

**统一异常处理**：在 `config/` 下新增 `GlobalExceptionHandler.java`，替代每个 Controller 里重复的 try-catch：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "操作失败: " + e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
```

加了这个之后，Controller 里的 try-catch 就可以简化掉，代码更干净。这条如果时间不够可以跳过。

---

## 二、报告撰写指南

报告是这次的重头戏。以下是可以直接用的结构和素材。

### 2.1 建议结构

| 章节 | 建议页数 | 核心内容 |
|---|---|---|
| 摘要 | 半页 | 一句话说清楚：基于 Spring Boot + AI 的作文智能评分系统 |
| 选题背景与意义 | 1-2 页 | AI+教育趋势、传统作文批改痛点（效率低、主观性强） |
| 需求分析 | 2-3 页 | 用例图、角色分析（学生/教师）、功能需求列表 |
| 系统设计 | 3-4 页 | 架构图、ER 图、API 接口表、技术选型说明 |
| 系统实现 | 4-6 页 | 核心功能截图 + 关键代码片段 + 技术说明 |
| 测试 | 1-2 页 | 测试策略 + 测试用例表 + 结果截图 |
| 总结与展望 | 1 页 | 已完成内容 + 不足 + 后续方向 |
| 参考文献 | 半页 | Spring Boot、Spring Security、DeepSeek 相关 |

### 2.2 各章节可直接用的素材

#### 选题背景

- 传统作文批改：一个语文老师带 2-3 个班，每篇作文精批需 10-15 分钟，100 个学生就是 20+ 小时
- AI 辅助评分：一致性高、即时反馈、多维度评价（不只一个总分）
- 2023-2025 年大语言模型（LLM）能力成熟，中文理解和生成能力已接近人类教师水平

#### 系统架构图（用文字描述，你用 draw.io 画）

```
┌──────────────────────────────────────────┐
│              浏览器（前端）                │
│   login.html   student.html  teacher.html │
├──────────────────────────────────────────┤
│         Spring Security (JWT 过滤器)       │
├──────────────────────────────────────────┤
│   AuthController  StudentController       │
│          TeacherController                │
├──────────────────────────────────────────┤
│   EssayService   AIScoringService         │
│   ScoringRuleService                      │
├──────────────────────────────────────────┤
│   5 个 JPA Repository                     │
├──────────────────────────────────────────┤
│   MySQL 数据库                            │
├──────────────────────────────────────────┤
│   DeepSeek API (deepseek-chat 模型)          │
└──────────────────────────────────────────┘
```

#### ER 图（6 张表）

- **users**：id, username, password(BCrypt), role(STUDENT/TEACHER), student_id, display_name, enabled
- **essays**：id, student_id, student_name, title, content(TEXT), grade, essay_type, word_count, submit_time
- **scores**：id, essay_id(FK, 1:1), content_score(30), structure_score(25), language_score(25), creativity_score(20), total_score(100), + 各维度评语
- **feedbacks**：id, essay_id(FK, N:1), feedback_type, position, issue, suggestion, improved_example, severity
- **scoring_rules**：id, rule_name, applicable_grade, applicable_essay_type, dimension, weight, criteria, enabled

#### API 接口表

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | /api/auth/register | 用户注册 | 公开 |
| POST | /api/auth/login | 用户登录，返回 JWT | 公开 |
| GET | /api/auth/me | 获取当前用户信息 | 登录 |
| POST | /api/student/essay/submit | 提交作文，返回 AI 评分 | 登录 |
| GET | /api/student/essay/{id}/result | 查看评分详情 | 登录 |
| GET | /api/student/essays?studentId= | 我的历史作文列表 | 登录 |
| GET | /api/teacher/essays | 所有学生作文列表 | 教师 |
| GET | /api/teacher/rules | 评分规则列表 | 教师 |
| POST | /api/teacher/rules | 创建评分规则 | 教师 |
| PUT | /api/teacher/rules/{id} | 更新规则 | 教师 |
| DELETE | /api/teacher/rules/{id} | 删除规则 | 教师 |
| PUT | /api/teacher/rules/{id}/toggle | 启用/禁用规则 | 教师 |
| GET | /api/teacher/statistics | 统计数据（篇数/人数/均分） | 教师 |

#### 技术选型说明

| 技术 | 选型理由 |
|---|---|
| Spring Boot 3.0.2 | Java 生态最主流的企业级框架，自动配置 + Starter 生态 |
| Spring Security + JWT | 无状态认证，适合前后端分离；BCrypt 密码加密 |
| Spring Data JPA | 自动生成 SQL，减少 DAO 层代码，支持方法命名查询 |
| MySQL | 主流关系型数据库，支持中文编码 |
| DeepSeek | 国产大模型、中文能力强、OpenAI 兼容接口、性价比高 |
| Lombok | 减少样板代码（getter/setter/构造器） |
| Chart.js | 轻量级前端图表库，雷达图展示多维度评分 |
| Maven | 依赖管理、多环境构建 |

#### 核心代码片段（报告中贴的）

**AI 评分四维度 Prompt 设计**：
```
你是一位经验丰富的语文教师，请对以下学生作文进行专业评分。
【评分要求】四个维度：
1. 内容（满分30分）：主题明确、内容充实、观点正确
2. 结构（满分25分）：层次清晰、逻辑严密、结构完整
3. 语言（满分25分）：表达准确、用词恰当、语句流畅
4. 创意（满分20分）：立意新颖、角度独特、富有想象力
输出格式：JSON（contentScore, structureScore, languageScore, creativityScore, 各维度评语, 综合评语）
```

**JWT 认证流程**（报告中用流程图展示）：
```
用户登录 → AuthController 验证用户名密码 → JwtService 生成 Token → 返回前端 localStorage
后续请求 → JwtAuthenticationFilter 拦截 → 解析 Token → 加载用户信息 → Controller 处理
```

### 2.3 截图清单（提前准备好）

| 序号 | 截图内容 | 对应报告位置 |
|---|---|---|
| 1 | 登录页面（学生） | 系统实现 |
| 2 | 作文提交表单（含填写内容） | 系统实现 |
| 3 | **AI 评分结果页**（四维分数卡片 + 雷达图） | 核心亮点 |
| 4 | AI 反馈列表（语法/表达/结构建议） | 核心亮点 |
| 5 | 历史作文列表 | 系统实现 |
| 6 | 教师端 - 全部作文列表 | 系统实现 |
| 7 | 教师端 - 评分规则管理（含 CRUD 弹窗） | 系统实现 |
| 8 | 教师端 - 统计面板 | 系统实现 |
| 9 | 数据库表结构（MySQL Workbench/Navicat 截图） | 系统设计 |

---

## 三、演示准备

### 3.1 演示流程（建议 5-6 分钟）

**0:00-0:30 — 项目概述**
> "这是一个基于大语言模型的作文智能评分系统。学生在网页端提交作文，AI 自动从内容、结构、语言、创意四个维度打分，并生成逐条改进建议。教师端可以管理评分规则、查看统计。"

**0:30-2:00 — 学生端演示（核心）**
1. 打开 `student.html`，登录学生账号
2. 粘贴一篇准备好的作文（提前准备 300-500 字）
3. 选择年级和文体，点击提交
4. 展示评分结果：四个维度的分数卡片 + 雷达图
5. 展示 AI 反馈：语法纠正、表达优化建议
6. 查看历史作文列表

**2:00-3:30 — 教师端演示**
1. 切换到无痕窗口，打开 `teacher.html`，登录教师账号
2. 查看所有学生提交的作文列表
3. 新增一条评分规则（演示 CRUD）
4. 查看统计面板（篇数、人数、平均分）

**3:30-5:00 — 技术亮点讲解**
> 挑 2-3 个点深入讲：
> - JWT 认证 + 角色权限是怎么实现的
> - AI 评分 Prompt 设计思路（为什么选四个维度、满分分配逻辑）
> - Mock 模式的降级策略（API Key 未配时如何兜底）
> - 数据库 ER 设计（Essay 1:1 Score → 1:N Feedback）

**5:00-5:30 — 总结**
> 一句话收尾 + "请老师指正"

### 3.2 应急预案

| 风险 | 应对 |
|---|---|
| DeepSeek API 超时/挂掉 | **优先用 Mock 模式演示**，Mock 返回的假数据一样能展示全部 UI 效果 |
| 现场无网络 | 本地 MySQL + Mock 模式，完全离线可跑 |
| 数据库连不上 | 提前改配置切回 H2 内存数据库，零依赖启动 |
| 紧张忘词 | 在另一个屏幕开个 Markdown 提词器 |

### 3.3 演示前检查清单

- [ ] Mock 模式已测试通过（注释掉 API Key 或设成 `your-api-key-here`）
- [ ] MySQL 本地库已有测试数据，或已切换 H2
- [ ] 学生账号和教师账号已提前注册好（演示时直接登录，不现场注册）
- [ ] 作文内容已提前准备好（复制粘贴用，不现场打字）
- [ ] 浏览器已清空缓存/Cookie（演示用无痕窗口更干净）
- [ ] 备用录屏已准备好（如果现场翻车可以播放）

---

## 四、常见答辩问题预判

| 问题 | 建议回答方向 |
|---|---|
| "AI 评分和人工评分一致性怎么样？" | 坦诚：目前没有做大规模对比实验，这是后续方向。但从 Prompt 设计和四维度框架来看，AI 在语法、结构等客观维度上表现稳定 |
| "安全性怎么保证的？" | JWT + BCrypt + 基于角色的访问控制（hasRole），密码不存明文，Token 24 小时过期 |
| "为什么选 DeepSeek？" | 国产大模型、中文能力强、OpenAI 兼容接口易于集成、性价比高 |
| "测试做得怎么样？" | 坦率说覆盖不足，目前以功能测试和手动测试为主，后续需要用 Mockito + JUnit 补单元测试 |
| "前端为什么没用框架？" | 项目规模适中，原生 JS + Chart.js 足够，引入框架（Vue/React）反而增加复杂度。后续功能增加后可以考虑迁移 |
| "你这个项目有什么用？" | 帮助语文老师减轻批改负担，尤其是过程性写作中的初稿反馈；学生也能即时获得改进方向 |

---

## 五、总结

三天时间分配建议：

| 时间 | 做什么 |
|---|---|
| **周二** | 修安全问题（30min）+ 准备截图（1h）+ 开始写报告（2h 写前三章） |
| **周三** | 写完报告全文 + 画架构图/ER 图/流程图 |
| **周四** | 报告审查修改 + 演示走场 3 遍以上 |
| **周五上午** | 最后一遍走场 + 提交报告 + 演示答辩 |

**核心原则：代码修掉明显的坑就行，报告和演示是这次的主角。**
