# 多阶段构建：先编译打包，再运行，镜像更小
# ── 阶段一：构建 ──
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# 先复制 pom，利用 Docker 层缓存下载依赖
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
# 再复制源码并打包（跳过测试，测试在 CI 里跑）
COPY src ./src
RUN mvn -q -B package -DskipTests

# ── 阶段二：运行 ──
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# JWT_SECRET / DEEPSEEK_API_KEY 通过环境变量注入，不写死在镜像里
ENTRYPOINT ["java", "-jar", "app.jar"]
