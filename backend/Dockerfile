# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
# 避免有些插件在 go-offline 時失敗導致中斷，我們直接依賴打包階段來下載就好，以增加穩定性。
# 移除 RUN mvn dependency:go-offline
COPY src ./src
# 編譯並打包 (忽略測試)
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
