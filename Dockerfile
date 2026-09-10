# syntax=docker/dockerfile:1

# Spring Boot JAR는 CPU 아키텍처와 무관하다. 시간이 오래 걸리는 빌드는 CI 러너에서
# 실행하고, Buildx가 요청한 실행 아키텍처(운영 amd64, 개발 arm64) 이미지를 만든다.
FROM --platform=$BUILDPLATFORM gradle:8.8-jdk21-jammy AS builder
WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN chmod +x gradlew
RUN ./gradlew --no-daemon dependencies || true

COPY . .
RUN chmod +x gradlew
RUN ./gradlew --no-daemon clean bootJar

# 이 런타임 이미지는 amd64와 arm64를 모두 지원한다.
FROM eclipse-temurin:21-jre-jammy
ENV TZ=Asia/Seoul
WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev}"]
