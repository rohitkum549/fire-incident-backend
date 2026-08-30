# Stage 1: Build the application using Maven
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy dependency definition and resolve dependencies
COPY pom.xml .
COPY checkstyle-suppressions.xml .
RUN mvn dependency:go-offline -B

# Copy source directory and package
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run the application in a lightweight JRE container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root group and user for security hardening
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# Copy the built jar from the builder stage
COPY --from=builder /build/target/fire-management-backend-*.jar app.jar

# Expose port and run using optimal garbage collection and container-aware memory limits
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
