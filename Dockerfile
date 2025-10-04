# Java 17 (Spring Boot 3 bilan ishlaydi)
FROM openjdk:17-jdk-slim

# App jarni image ichiga nusxalash
COPY target/*.jar app.jar

# Run
ENTRYPOINT ["java","-jar","/app.jar"]
