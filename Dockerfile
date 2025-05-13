FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the jar file
COPY build/libs/*.jar app.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
