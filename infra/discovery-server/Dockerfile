FROM eclipse-temurin:21-jre

COPY infra/discovery-server/target/discovery-server-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]