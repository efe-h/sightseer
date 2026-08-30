# Stage 1: build the Spring Boot executable JAR
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy the Gradle wrapper and build configuration first.
# Docker can cache downloaded dependencies when source code changes.
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

# Download dependencies in a cacheable layer.
RUN ./gradlew dependencies --no-daemon

# Copy the application source code.
COPY src src

# Build the executable JAR without rerunning tests.
# Tests are already run separately by CI and may require Docker/Testcontainers.
RUN ./gradlew bootJar --no-daemon


# Stage 2: run the application in a smaller Java image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Run the container as an unprivileged user.
RUN groupadd --system sightseer \
    && useradd --system \
    --gid sightseer \
    --no-create-home \
    sightseer

COPY --from=build /app/build/libs/*.jar app.jar

USER sightseer

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]