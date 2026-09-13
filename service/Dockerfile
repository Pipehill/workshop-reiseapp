FROM docker.io/library/eclipse-temurin:25.0.4_7-jre-noble

WORKDIR /app

# Run mvn clean verify from the repository root before building this image.
COPY target/service-*.jar /app/service.jar

USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/service.jar"]
