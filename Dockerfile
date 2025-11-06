# ====== build stage ======
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
# копируем сначала pom для кеша зависимостей
COPY pom.xml .
RUN mvn -B -q -e -DskipTests dependency:go-offline

# копируем исходники и собираем fat jar
COPY src ./src
RUN mvn -B -q -DskipTests spring-boot:repackage

# найдём единственный repackage-jar
RUN JAR_FILE=$(ls target/*-SNAPSHOT.jar 2>/dev/null || ls target/*.jar) && \
    echo "JAR_FILE=$JAR_FILE" > /jarfile.env

# ====== runtime stage ======
FROM gcr.io/distroless/java17-debian12:nonroot
WORKDIR /app
ENV TZ=Etc/UTC \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError" \
    SPRING_PROFILES_ACTIVE=dev \
    SERVER_PORT=8080

# переменные подключения к БД (переопредели в compose/секретах)
ENV DB_URL="jdbc:postgresql://postgres:5432/edu" \
    DB_USER="edu" \
    DB_PASSWORD="edu"

# скопируем jar из build-стадии
COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080
USER nonroot:nonroot
ENTRYPOINT ["java","-jar","/app/app.jar"]
