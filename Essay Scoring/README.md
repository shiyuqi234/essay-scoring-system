# 智能作文评分与反馈系统

基于 Spring Boot + DeepSeek API 的智能作文评分与反馈系统，融合 AI 大模型能力，能够自动对学生的作文进行语法检查、内容评分，并提供结构化反馈和改进建议。

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
- **DeepSeek API** - AI 大模型接口（OpenAI 兼容，中文能力强）
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
- DeepSeek API Key

### 安装步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd "Essay Scoring"
```

2. **配置 DeepSeek API Key**

编辑 `src/main/resources/application.yml`，替换 API Key：
```yaml
spring:
  cloud:
    ai:
      deepseek:
        api-key: your-api-key-here
```

或者设置环境变量：
```bash
# Windows
set DEEPSEEK_API_KEY=your-api-key-here

# Linux/Mac
export DEEPSEEK_API_KEY=your-api-key-here
```

**注意**：
- 如果未配置 API Key，系统将使用模拟的 ChatClient 进行测试
- 实际使用时需要配置有效的 DeepSeek API Key
- 获取 API Key：访问 [DeepSeek 开放平台](https://platform.deepseek.com/) 注册并获取

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
│   │   │       ├── EssayScoringApplication.java   # 启动类
│   │   │       ├── config/            # 配置层
│   │   │       │   ├── AIConfig.java             # AI API 配置（DeepSeek/Mock）
│   │   │       │   ├── SecurityConfig.java       # Spring Security + JWT 配置
│   │   │       │   ├── JwtService.java           # JWT 生成与解析
│   │   │       │   ├── JwtAuthenticationFilter.java  # JWT 认证过滤器
│   │   │       │   ├── JacksonConfig.java        # JSON 序列化配置
│   │   │       │   ├── DataInitializer.java      # 演示账号初始化
│   │   │       │   └── GlobalExceptionHandler.java   # 全局异常处理
│   │   │       ├── controller/        # 控制器层
│   │   │       │   ├── AuthController.java       # 注册/登录/状态检查
│   │   │       │   ├── StudentController.java    # 学生端 API
│   │   │       │   ├── TeacherController.java    # 教师端 API
│   │   │       │   └── TestController.java       # 诊断接口
│   │   │       ├── service/           # 服务层
│   │   │       │   ├── ChatClient.java           # AI 调用接口
│   │   │       │   ├── AIScoringService.java     # AI 评分引擎
│   │   │       │   ├── EssayService.java         # 作文管理服务
│   │   │       │   └── ScoringRuleService.java   # 评分规则服务
│   │   │       ├── repository/        # 数据访问层
│   │   │       │   ├── EssayRepository.java
│   │   │       │   ├── ScoreRepository.java
│   │   │       │   ├── FeedbackRepository.java
│   │   │       │   ├── ScoringRuleRepository.java
│   │   │       │   └── UserAccountRepository.java
│   │   │       ├── entity/            # 实体类
│   │   │       │   ├── Essay.java
│   │   │       │   ├── Score.java
│   │   │       │   ├── Feedback.java
│   │   │       │   ├── ScoringRule.java
│   │   │       │   ├── UserAccount.java
│   │   │       │   └── UserRole.java
│   │   │       ├── dto/               # 数据传输对象
│   │   │       │   ├── EssaySubmitDTO.java
│   │   │       │   ├── ScoringResultDTO.java
│   │   │       │   ├── LoginRequest.java
│   │   │       │   ├── RegisterRequest.java
│   │   │       │   ├── ScoreUpdateRequest.java
│   │   │       │   └── ApiResponse.java
│   │   │       └── util/
│   │   │           └── PasswordGenerator.java   # BCrypt 密码哈希工具
│   │   └── resources/
│   │       ├── application.yml        # 配置文件
│   │       └── static/                # 前端页面
│   │           ├── index.html         # 登录/注册页
│   │           ├── student.html       # 学生端
│   │           └── teacher.html       # 教师端
│   └── test/                          # 测试代码
├── pom.xml                            # Maven 配置
└── README.md                          # 项目文档
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

系统默认使用 `deepseek-chat` 模型，可在 `application.yml` 中修改：

```yaml
spring:
  cloud:
    ai:
      deepseek:
        api-key: your-api-key-here
        chat:
          options:
            model: deepseek-chat          # DeepSeek 对话模型
            temperature: 0.3              # 温度参数（0-1，越低越稳定）
            max-tokens: 2000              # 最大输出 token 数
```

**DeepSeek API 说明**：
- 使用 DeepSeek OpenAI 兼容接口，通过标准 HTTP 调用
- 如果 API Key 未配置，系统会自动使用 MockChatClient
- 支持 `deepseek-chat` 和 `deepseek-reasoner` 模型

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
- 基于 DeepSeek 大模型
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

## 🔒 安全配置

### 环境变量（必须配置）

系统通过环境变量管理所有敏感信息，**不应在配置文件中硬编码**：

| 环境变量 | 说明 | 默认值 |
|----------|------|--------|
| `DEEPSEEK_API_KEY` | DeepSeek API 密钥 | `your-api-key-here`（未配置时自动使用 Mock 模式） |
| `JWT_SECRET` | JWT 签名密钥（Base64） | 开发默认值（生产环境必须更换） |
| `CORS_ORIGINS` | 允许的前端来源 | `http://localhost:8080` |

**Windows 设置方式**：
```powershell
setx DEEPSEEK_API_KEY "sk-xxxxxxxxxxxxxxxx"
setx JWT_SECRET "你的Base64密钥"
```

**Linux/Mac 设置方式**：
```bash
export DEEPSEEK_API_KEY="sk-xxxxxxxxxxxxxxxx"
export JWT_SECRET="你的Base64密钥"
```

### 安全措施

1. **API Key 保护** — 通过环境变量注入，不写入配置文件或提交 Git
2. **JWT 认证** — 无状态 Token 认证，24 小时过期，BCrypt 密码加密
3. **角色权限** — 基于 `@hasRole` 的访问控制，学生/教师权限隔离
4. **CORS 限制** — 允许的来源通过 `CORS_ORIGINS` 环境变量配置，非 `*` 通配
5. **数据库安全** — 数据库文件已加入 `.gitignore`，不纳入版本控制
6. **输入验证** — 使用 `@Valid` 注解校验所有用户输入
7. **全局异常处理** — 统一捕获异常，不泄露内部错误详情到前端

### 演示账号

系统启动时自动创建（仅当不存在时）：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| `student1` | `123456` | 学生（张三，学号 S2024001） |
| `teacher1` | `123456` | 教师（李老师） |

## 🐛 故障排查

### AI 调用失败
- 检查 API Key 是否正确配置
- 确认网络连接正常
- 查看日志中的错误信息
- 如果使用 MockChatClient，检查 `AIConfig.java` 配置
- 确认 DeepSeek API Key 是否正确配置

### 数据库连接问题
- 检查数据库配置是否正确
- 确认数据库服务是否启动
- 查看 H2 控制台连接信息（http://localhost:8080/h2-console）
- H2 连接信息：
  - JDBC URL: `jdbc:h2:mem:essayscoring`
  - 用户名: `sa`
  - 密码: （空）

### 依赖问题
- 如果 DeepSeek API Key 未配置，系统会自动使用 MockChatClient
- 检查 `AIConfig.java` 中的 API 地址和配置

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

**注意**：使用本系统需要有效的 DeepSeek API Key。请访问 [DeepSeek 开放平台](https://platform.deepseek.com/) 获取 API Key。
