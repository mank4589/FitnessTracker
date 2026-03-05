# Multi-stage build for Spring Boot application with JavaFX
# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

# Install Maven
RUN apk add --no-cache maven

# Set working directory
WORKDIR /build

# Copy Maven wrapper and pom.xml first for better layer caching
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Download dependencies (cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B || true

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Verify the JAR was created
RUN ls -la target/

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Create data directory for H2 database
RUN mkdir -p /app/data && chown -R appuser:appgroup /app

# Copy the built JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Change ownership of the application files
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port 8080 for Spring Boot
EXPOSE 8080

# Expose port 8082 for H2 console (if enabled)
EXPOSE 8082

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Note: JavaFX requires a display server. For headless mode or GUI access:
# - Use X11 forwarding: docker run -e DISPLAY=$DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix
# - Use VNC server in container
# - Run in headless mode if the app supports it

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
