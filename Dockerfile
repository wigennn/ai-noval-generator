# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy pom and download dependencies
COPY pom.xml .
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn package -DskipTests -B

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache dumb-init
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["dumb-init", "java", "-jar", "app.jar"]
