FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./
COPY management/build.gradle management/build.gradle
COPY api-gateway/build.gradle api-gateway/build.gradle
COPY notification-service/build.gradle notification-service/build.gradle
COPY billing-service/build.gradle billing-service/build.gradle
RUN chmod +x gradlew
COPY notification-service/src notification-service/src
RUN ./gradlew :notification-service:bootJar --no-daemon -x test

FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
COPY --from=build /app/notification-service/build/libs/*.jar notification-service.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "notification-service.jar"]
