# ───────── Stage 1: build ─────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Cache dependency trước để build lại nhanh khi chỉ sửa code
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ───────── Stage 2: run ─────────
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --uid 1001 indmart \
 && mkdir -p /app/UploadFiles \
 && chown -R indmart:indmart /app
USER indmart

COPY --from=build --chown=indmart:indmart /build/target/web-ind-mart-api-*.jar /app/app.jar

EXPOSE 9090
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
