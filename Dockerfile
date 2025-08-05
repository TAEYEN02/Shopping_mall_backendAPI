# 빌드 스테이지
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x shopping/gradlew
RUN cd shopping && ./gradlew build && ls -l build/libs/

# 실행 스테이지
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/shopping/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
