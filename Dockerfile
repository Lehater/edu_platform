# ===== build =====
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests clean package spring-boot:repackage
RUN set -eux; \
    JAR_FILE="$(ls target/*.jar | grep -v original | grep -v sources | grep -v javadoc | head -n1)"; \
    cp "${JAR_FILE}" /workspace/app.jar

# ===== runtime =====
FROM gcr.io/distroless/java17-debian12:nonroot
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=dev \
    DB_URL="jdbc:postgresql://postgres:5432/edu" \
    DB_USER="edu" \
    DB_PASSWORD="edu"
COPY --from=build /workspace/app.jar /app/app.jar
EXPOSE 8080
USER nonroot:nonroot
ENTRYPOINT ["java","-jar","/app/app.jar"]