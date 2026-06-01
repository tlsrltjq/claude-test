# ── Stage 1: Build ────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Gradle wrapper + dependency manifests (캐시 레이어)
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q

# 소스 빌드
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon -q

# ── Stage 2: Runtime ───────────────────────────────────────────
# Debian 기반 — LibreOffice 패키지 지원을 위해 Alpine 대신 사용
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 전용 시스템 유저 (root 비실행)
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# LibreOffice + 한글 폰트 (오피스 파일 PDF 변환용)
RUN apt-get update && apt-get install -y --no-install-recommends \
        libreoffice-core \
        libreoffice-writer \
        libreoffice-calc \
        libreoffice-impress \
        fonts-nanum \
        fonts-nanum-coding \
        fontconfig \
    && fc-cache -f \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/libs/*.jar app.jar

# 업로드 디렉터리 생성 (볼륨 마운트 전 소유권 확보)
RUN mkdir -p /data/uploads && chown -R appuser:appgroup /data /app

USER appuser
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
  CMD wget -qO- http://localhost:8080/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
