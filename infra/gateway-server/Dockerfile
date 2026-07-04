FROM eclipse-temurin:21-jre

COPY infra/gateway-server/target/gateway-server-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]