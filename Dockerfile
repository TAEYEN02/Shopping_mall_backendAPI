# 빌드 스테이지
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x shopping/gradlew
RUN cd shopping && ./gradlew build

# 실행 스테이지
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/shopping/build/libs/shopping-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
