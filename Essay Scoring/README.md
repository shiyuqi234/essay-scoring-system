# 智能作文评分与反馈系统

基于 Spring AI Alibaba 的智能作文评分与反馈系统，融合 NLP 与 AI 大模型能力，能够自动对学生的作文进行语法检查、内容评分，并提供结构化反馈和改进建议。

## 📋 项目功能

### 学生端功能
- ✅ 提交作文（支持文本输入或粘贴）
- ✅ 选择年级和文体类型
- ✅ AI 自动评分（多维度评分）
- ✅ 评分结果可视化（雷达图）
- ✅ 智能反馈生成（改进建议、错误标注、优化示例）

### 教师端功能
- ✅ 查看所有学生作文
- ✅ 管理评分规则和权重
- ✅ 统计分析（总作文数、学生数、平均分等）
- ✅ 启用/禁用评分规则

## 🏗️ 技术架构

### 后端技术栈
- **Spring Boot 3.0.2** - 核心框架
- **Spring AI** - AI 应用框架（集成阿里云百炼平台 DashScope）
- **Spring Data JPA** - 数据持久化
- **H2 Database** - 开发环境数据库（可替换为 MySQL）
- **Lombok** - 简化代码

### 前端技术栈
- **HTML5 + CSS3** - 现代化 UI 设计
- **JavaScript (ES6+)** - 前端交互
- **Chart.js** - 数据可视化（雷达图）

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- 阿里云百炼平台 API Key

### 安装步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd "Essay Scoring"
```

2. **配置阿里云百炼平台 API Key**

编辑 `src/main/resources/application.yml`，替换 API Key：
```yaml
spring:
  cloud:
    ai:
      dashscope:
        api-key: your-api-key-here
```

或者设置环境变量：
```bash
# Windows
set DASHSCOPE_API_KEY=your-api-key-here

# Linux/Mac
export DASHSCOPE_API_KEY=your-api-key-here
```

**注意**：
- 如果未配置 API Key，系统将使用模拟的 ChatClient 进行测试
- 实际使用时需要配置有效的阿里云百炼平台 API Key
- 获取 API Key：访问 [阿里云百炼平台](https://dashscope.aliyun.com/) 注册并获取

3. **编译项目**
```bash
mvn clean compile
```

4. **运行项目**
```bash
mvn spring-boot:run
```

5. **访问系统**
- 学生端：http://localhost:8080/student.html
- 教师端：http://localhost:8080/teacher.html
- H2 控制台：http://localhost:8080/h2-console

## 📁 项目结构

```
Essay Scoring/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/study/ai/essayscoring/
│   │   │       ├── controller/          # 控制器层
│   │   │       │   ├── StudentController.java
│   │   │       │   └── TeacherController.java
│   │   │       ├── service/             # 服务层
│   │   │       │   ├── AIScoringService.java
│   │   │       │   ├── EssayService.java
│   │   │       │   └── ScoringRuleService.java
│   │   │       ├── repository/          # 数据访问层
│   │   │       │   ├── EssayRepository.java
│   │   │       │   ├── ScoreRepository.java
│   │   │       │   ├── FeedbackRepository.java
│   │   │       │   └── ScoringRuleRepository.java
│   │   │       ├── entity/              # 实体类
│   │   │       │   ├── Essay.java
│   │   │       │   ├── Score.java
│   │   │       │   ├── Feedback.java
│   │   │       │   └── ScoringRule.java
│   │   │       └── dto/                 # 数据传输对象
│   │   │           ├── EssaySubmitDTO.java
│   │   │           └── ScoringResultDTO.java
│   │   └── resources/
│   │       ├── application.yml          # 配置文件
│   │       └── static/                  # 静态资源
│   │           ├── student.html         # 学生端页面
│   │           └── teacher.html        # 教师端页面
│   └── test/                            # 测试代码
├── pom.xml                              # Maven 配置
└── README.md                            # 项目文档
```

## 🔧 配置说明

### 数据库配置

默认使用 H2 内存数据库，如需切换到 MySQL：

1. 修改 `pom.xml`，确保包含 MySQL 驱动（已包含）
2. 修改 `application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/essayscoring?useUnicode=true&characterEncoding=utf8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: your-password
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

### AI 模型配置

系统默认使用 `qwen-turbo` 模型，可在 `application.yml` 中修改：

```yaml
spring:
  cloud:
    ai:
      dashscope:
        api-key: your-api-key-here
        chat:
          options:
            model: qwen-turbo  # 可选：qwen-turbo, qwen-plus, qwen-max
            temperature: 0.3  # 温度参数（0-1，越低越稳定）
            max-tokens: 2000  # 最大输出 token 数
```

**Spring AI 依赖说明**：
- 项目使用 Spring AI 框架集成阿里云百炼平台
- 如果 `spring-ai-dashscope-spring-boot-starter` 依赖不可用，系统会自动使用 MockChatClient
- 可以根据实际使用的 Spring AI 版本调整 `AIConfig.java` 中的配置
- 建议使用 Spring AI 1.0.0-M4 或更高版本

## 📊 评分维度

系统从以下四个维度对作文进行评分：

1. **内容（满分30分）**
   - 主题明确
   - 内容充实
   - 观点正确

2. **结构（满分25分）**
   - 层次清晰
   - 逻辑严密
   - 结构完整

3. **语言（满分25分）**
   - 表达准确
   - 用词恰当
   - 语句流畅

4. **创意（满分20分）**
   - 立意新颖
   - 角度独特
   - 富有想象力

## 🔌 API 接口

### 学生端 API

#### 提交作文
```
POST /api/student/essay/submit
Content-Type: application/json

{
  "studentName": "张三",
  "studentId": "S001",
  "title": "我的家乡",
  "content": "作文内容...",
  "grade": "小学五年级",
  "essayType": "记叙文"
}
```

#### 查询评分结果
```
GET /api/student/essay/{id}/result
```

#### 查询学生所有作文
```
GET /api/student/essays?studentId=S001
```

### 教师端 API

#### 获取所有作文
```
GET /api/teacher/essays
```

#### 获取所有评分规则
```
GET /api/teacher/rules
```

#### 创建评分规则
```
POST /api/teacher/rules
Content-Type: application/json

{
  "ruleName": "内容评分规则",
  "dimension": "content",
  "weight": 0.3,
  "criteria": "评分标准描述..."
}
```

#### 更新评分规则
```
PUT /api/teacher/rules/{id}
```

#### 删除评分规则
```
DELETE /api/teacher/rules/{id}
```

#### 获取统计信息
```
GET /api/teacher/statistics
```

## 🎨 功能特性

### AI 评分引擎
- 基于阿里云百炼平台 Qwen 大模型
- 多维度智能评分
- 结构化 JSON 输出解析
- 异常处理和默认评分机制

### 智能反馈生成
- 语法错误检测
- 表达优化建议
- 结构改进建议
- 词汇使用建议
- 提供优化示例句子

### 数据可视化
- 雷达图展示各维度得分
- 直观的评分卡片展示
- 反馈建议分类展示

## 🔒 安全建议

1. **API Key 保护**
   - 不要将 API Key 提交到版本控制系统
   - 使用环境变量或配置中心管理敏感信息
   - 将 `application.yml` 中的 API Key 设置为环境变量引用

2. **数据验证**
   - 所有用户输入都经过验证（使用 `@Valid` 注解）
   - 防止 SQL 注入和 XSS 攻击
   - 前端和后端双重验证

3. **访问控制**
   - 建议添加用户认证和授权机制（如 Spring Security）
   - 区分学生端和教师端权限
   - 实现 JWT Token 认证

## 🐛 故障排查

### AI 调用失败
- 检查 API Key 是否正确配置
- 确认网络连接正常
- 查看日志中的错误信息
- 如果使用 MockChatClient，检查 `AIConfig.java` 配置
- 确认 Spring AI DashScope 依赖是否正确引入

### 数据库连接问题
- 检查数据库配置是否正确
- 确认数据库服务是否启动
- 查看 H2 控制台连接信息（http://localhost:8080/h2-console）
- H2 连接信息：
  - JDBC URL: `jdbc:h2:mem:essayscoring`
  - 用户名: `sa`
  - 密码: （空）

### 依赖问题
- 如果 Spring AI DashScope 依赖不可用，系统会自动使用 MockChatClient
- 可以手动添加 Spring AI 依赖到 `pom.xml`
- 或者直接使用阿里云 DashScope SDK 替代

## 📝 开发计划

- [ ] 添加用户认证和授权
- [ ] 支持批量导入作文
- [ ] 增加更多可视化图表
- [ ] 支持导出评分报告
- [ ] 添加作文历史对比功能
- [ ] 支持自定义评分模板

## 📄 许可证

本项目采用 MIT 许可证。

## 👥 贡献

欢迎提交 Issue 和 Pull Request！

## 📧 联系方式

如有问题或建议，请通过 Issue 反馈。

---

**注意**：使用本系统需要有效的阿里云百炼平台 API Key。请访问 [阿里云百炼平台](https://dashscope.aliyun.com/) 获取 API Key。
