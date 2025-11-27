
# ---------- Stage 1: Build ----------
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace

# Copy only files needed for dependency download first
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw mvnw

# Fix permission issue for mvnw
RUN chmod +x mvnw

# Pre-fetch dependencies
RUN ./mvnw -q -DskipTests dependency:go-offline

# Copy source and build the jar
COPY src src
RUN ./mvnw -q -DskipTests clean package

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jdk-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
