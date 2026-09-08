FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./
COPY management/build.gradle management/build.gradle
COPY api-gateway/build.gradle api-gateway/build.gradle
COPY notification-service/build.gradle notification-service/build.gradle
COPY billing-service/build.gradle billing-service/build.gradle
RUN chmod +x gradlew
COPY management/src management/src
RUN ./gradlew :management:bootJar --no-daemon -x test

FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
COPY --from=build /app/management/build/libs/*.jar skillcraft-management.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "skillcraft-management.jar"]
