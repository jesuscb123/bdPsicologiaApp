# ----- STAGE 1: build -----
FROM gradle:8.5-jdk17 AS build

WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

# ----- STAGE 2: run -----
FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY --from=build /app/build/libs/bdPsicologiaApp-0.0.1-SNAPSHOT.jar app.jar

# Render inyecta PORT (no tiene por qué ser 8080); Spring lo lee desde application.yaml.
EXPOSE 8080
ENV PORT=8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]