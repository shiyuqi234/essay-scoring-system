# 📝 智能作文评分与反馈系统

基于 **Spring Boot 3 + DeepSeek 大模型** 的智能作文评分系统，支持 AI 自动评分、多维度分析、结构化反馈生成，学生端和教师端分离。

> 🔗 **详细文档**：[Essay Scoring/README.md](Essay%20Scoring/README.md)

## ✨ 核心功能

| 学生端 | 教师端 |
|--------|--------|
| 提交作文（文本输入/粘贴） | 查看所有学生作文 |
| 选择年级和文体类型 | 管理评分规则和权重 |
| AI 四维度自动评分 | 手动调整评分 |
| 雷达图可视化 | 统计分析（篇数/人数/均分） |
| 智能反馈（语法/表达/结构建议） | 启用/禁用评分规则 |

## 🏗️ 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.0.2 |
| 安全认证 | Spring Security + JWT + BCrypt |
| 数据库 | H2（开发）/ MySQL（生产） |
| AI 引擎 | DeepSeek API（OpenAI 兼容接口） |
| 前端 | HTML5 + CSS3 + Chart.js（原生 JS） |
| 构建工具 | Maven |

## 🚀 快速启动

### 环境要求
- JDK 17+
- Maven 3.6+
- DeepSeek API Key（[免费获取](https://platform.deepseek.com/)）

### 3 步运行

```bash
# 1. 设置 API Key（Windows）
setx DEEPSEEK_API_KEY "sk-xxxxxxxxxxxxxxxx"

# 2. 编译
cd "Essay Scoring"
mvn clean compile

# 3. 启动
mvn spring-boot:run
```

访问：
- 学生端：http://localhost:8080/student.html
- 教师端：http://localhost:8080/teacher.html
- 登录页：http://localhost:8080/index.html

> 💡 **未配置 API Key 时自动使用 Mock 模式**，返回模拟评分数据，方便离线演示。

### 演示账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| `student1` | `123456` | 学生 |
| `teacher1` | `123456` | 教师 |

## 📊 评分维度

| 维度 | 满分 | 评估内容 |
|------|------|----------|
| 内容 | 30 分 | 主题明确、内容充实、观点正确 |
| 结构 | 25 分 | 层次清晰、逻辑严密、结构完整 |
| 语言 | 25 分 | 表达准确、用词恰当、语句流畅 |
| 创意 | 20 分 | 立意新颖、角度独特、富有想象力 |

## 🔒 安全配置

敏感信息通过环境变量管理：

| 环境变量 | 说明 |
|----------|------|
| `DEEPSEEK_API_KEY` | DeepSeek API 密钥 |
| `JWT_SECRET` | JWT 签名密钥（Base64） |
| `CORS_ORIGINS` | 允许的前端来源 |

详见 [Essay Scoring/README.md § 安全配置](Essay%20Scoring/README.md#-安全配置)

## 📁 项目结构

```
essay-scoring-system/
├── Essay Scoring/              # 主项目
│   ├── src/main/java/.../      # Java 源码
│   │   ├── config/             # Security、JWT、AI 配置
│   │   ├── controller/         # Auth、Student、Teacher API
│   │   ├── service/            # AI 评分、作文管理、规则服务
│   │   ├── entity/             # 6 个 JPA 实体
│   │   ├── repository/         # 5 个 Repository
│   │   └── dto/                # 请求/响应 DTO
│   ├── src/main/resources/
│   │   ├── application.yml     # 配置文件
│   │   └── static/             # 前端页面（3 个 HTML）
│   └── pom.xml
├── IMPROVEMENT_PLAN.md         # 改进计划与演示指南
└── README.md                   # 本文件
```

## 📄 许可证

MIT License
