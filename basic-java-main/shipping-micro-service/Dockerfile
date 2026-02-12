#Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy parent POM
COPY pom.xml .

# Copy module POMs
COPY gateway/pom.xml gateway/
COPY user-service/pom.xml user-service/
COPY shipping-micro-service/pom.xml shipping-micro-service/

# Copy source code (both modules needed for dependency resolution)
COPY gateway/src gateway/src
COPY user-service/src user-service/src
COPY shipping-micro-service/src shipping-micro-service/src

# Build the user-service module
RUN mvn clean package -pl shipping-micro-service -am -Dmaven.test.skip=true


# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Copy JAR from build stage
COPY --from=build /app/shipping-micro-service/target/shipping-micro-service-1.0.0.jar shipping-micro-service-1.0.0.jar


# Expose port
EXPOSE 8084

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -DSPRING_REDIS_HOST=$SPRING_REDIS_HOST -DSPRING_REDIS_PORT=$SPRING_REDIS_PORT -jar shipping-micro-service-1.0.0.jar"]