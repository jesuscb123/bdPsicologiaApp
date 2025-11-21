# ----- STAGE 1: build -----
FROM gradle:8.5-jdk21 AS build

WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

# ----- STAGE 2: run -----
FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]