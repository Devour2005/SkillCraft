FROM eclipse-temurin:23-jdk-alpine AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:23-jdk-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar ckillcraft.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "ckillcraft.jar"]

