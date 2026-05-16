FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy pom and sources for the fleet-monitor module
COPY fleet-monitor/pom.xml ./fleet-monitor/
COPY fleet-monitor/src ./fleet-monitor/src

# Build the application (skip tests for faster builds)
RUN mvn -f fleet-monitor/pom.xml -DskipTests package -B

FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/fleet-monitor/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
