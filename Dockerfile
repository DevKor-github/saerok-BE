# ===== Build stage =====
FROM gradle:8.8-jdk21 AS builder
WORKDIR /app

# Gradle 캐시 최적화를 위한 사전 레이어 (옵션)
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN chmod +x gradlew
RUN ./gradlew --no-daemon dependencies || true

# 실제 소스 복사 후 빌드
COPY . .
# COPY . . 로 gradlew가 덮어써질 수 있으므로 다시 실행 권한 부여
RUN chmod +x gradlew
# 테스트까지 돌리려면 'clean test bootJar' 로 변경
RUN ./gradlew --no-daemon clean bootJar

# ===== Runtime stage =====
FROM eclipse-temurin:21-jre
ENV TZ=Asia/Seoul \
    JAVA_OPTS="-Duser.timezone=Asia/Seoul -XX:+UseZGC -XX:MaxRAMPercentage=75"
WORKDIR /app

# (헬스체크용) curl 설치 - docker compose healthcheck가 컨테이너 내부에서 실행됨
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 빌드 산출물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
# SPRING_PROFILES_ACTIVE는 compose에서 주입(기본 dev)
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev}"]
