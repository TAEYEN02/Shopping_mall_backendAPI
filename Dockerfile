# 1단계: Build the app
FROM gradle:7.6-jdk17 AS build
WORKDIR /app

# 프로젝트 전체 복사
COPY . .

# gradlew에 실행 권한 부여 (Linux에서 권한 문제 방지)
RUN chmod +x ./gradlew

# build 실행
RUN ./gradlew build

# 2단계: Run the app with JRE only
FROM openjdk:17-jdk-slim
WORKDIR /app

# 빌드한 jar 파일만 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 앱 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
