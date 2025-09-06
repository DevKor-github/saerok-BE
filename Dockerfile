# ===== Build stage =====
FROM gradle:8.8-jdk21 AS builder
WORKDIR /app

# Gradle 캐시 최적화를 위해 먼저 의존성 레이어만 복사 (옵션)
# 필요하면 settings.gradle, gradle.properties 등도 함께 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN chmod +x gradlew

RUN ./gradlew --no-daemon dependencies || true

# 실제 소스 복사 후 빌드
COPY . .
# 테스트까지 돌리고 싶으면 'test bootJar', 빠르게는 'bootJar'만
RUN ./gradlew --no-daemon clean bootJar

# ===== Runtime stage =====
FROM eclipse-temurin:21-jre
ENV TZ=Asia/Seoul \
    JAVA_OPTS="-Duser.timezone=Asia/Seoul -XX:+UseZGC -XX:MaxRAMPercentage=75"
WORKDIR /app

# 산출 JAR 복사 (빌드 결과가 하나라면 다음 라인으로 충분)
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
# SPRING_PROFILES_ACTIVE는 compose에서 주입(기본 dev)
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev}"]
