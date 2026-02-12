# -------------------------------
# Stage 1: Build
# -------------------------------
FROM maven:3.9.5-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy parent POM
COPY pom.xml .

# Copy module POMs
COPY gateway/pom.xml gateway/
COPY user-service/pom.xml user-service/
COPY shipping-micro-service/pom.xml shipping-micro-service/

# Copy all source code
COPY gateway/src gateway/src
COPY user-service/src user-service/src
COPY shipping-micro-service/src shipping-micro-service/src

# Build gateway module (skip tests)
RUN mvn clean package -pl gateway -am -DskipTests

# -------------------------------
# Stage 2: Runtime
# -------------------------------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Environment variables
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Copy built JAR from build stage
COPY --from=build /app/gateway/target/gateway-*.jar app.jar

# Expose port
EXPOSE 8082

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
