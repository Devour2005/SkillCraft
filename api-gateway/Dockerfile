FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./
COPY management/build.gradle management/build.gradle
COPY api-gateway/build.gradle api-gateway/build.gradle
COPY notification-service/build.gradle notification-service/build.gradle
COPY billing-service/build.gradle billing-service/build.gradle
RUN chmod +x gradlew
COPY api-gateway/src api-gateway/src
RUN ./gradlew :api-gateway:bootJar --no-daemon -x test

FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
COPY --from=build /app/api-gateway/build/libs/*.jar api-gateway.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]
