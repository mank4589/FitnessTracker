# Multi-stage build for Spring Boot application with JavaFX
# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

# Install Maven
RUN apk add --no-cache maven

# Set working directory
WORKDIR /build

# Copy pom.xml first for better layer caching
COPY pom.xml ./

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

# Set environment variables — keep JVM memory under Railway's 512MB limit
ENV JAVA_OPTS="-Xmx256m -Xms128m -Djava.net.preferIPv4Stack=true"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
