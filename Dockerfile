# Stage 1: Build the application using Maven (Commented out for later use)
# FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
# WORKDIR /build
# COPY pom.xml .
# RUN mvn dependency:go-offline -B
# COPY src ./src
# RUN mvn clean package -DskipTests -B

# Stage 2: Run the application in a lightweight JRE container
# FROM eclipse-temurin:21-jre-alpine
# WORKDIR /app
# RUN addgroup -S appgroup && adduser -S appuser -G appgroup
# USER appuser:appgroup
# COPY --from=builder /build/target/fire-management-backend-*.jar app.jar
# EXPOSE 8080
# ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
