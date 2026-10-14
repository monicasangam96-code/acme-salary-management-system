# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:25-jre-alpine
ARG TZ=UTC
ENV TZ=${TZ}
RUN apk add --no-cache tini curl

WORKDIR /app
COPY --from=build /app/target/salary-mgmt-1.0.0.jar app.jar

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["tini", "--"]
CMD ["java", "-jar", "app.jar"]
