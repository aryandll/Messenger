
# ---------- Stage 1: Build ----------
FROM eclipse-temurin:17-jdk-alpine AS build

# Install tools needed by Maven wrapper (if any native deps are used, keep alpine for lightness)
WORKDIR /workspace

# Copy only files needed for dependency download first (speeds up layered builds)
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw mvnw

# Pre-fetch dependencies to leverage Docker layer caching
RUN ./mvnw -q -DskipTests dependency:go-offline

# Now copy source and build the jar
COPY src src
RUN ./mvnw -q -DskipTests clean package

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jdk-alpine

# A non-root user is recommended for security (optional)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy the built jar from the build stage
# Adjust the jar name if your artifactId/version differs
COPY --from=build /workspace/target/*.jar app.jar

# Expose the Spring Boot port
EXPOSE 8080

# Provide default JVM options via env (optional)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Start the app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
