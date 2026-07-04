FROM eclipse-temurin:21-jre

COPY infra/config-server/target/config-server-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]