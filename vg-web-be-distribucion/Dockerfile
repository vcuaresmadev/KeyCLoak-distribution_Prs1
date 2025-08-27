# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests clean package

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV JAVA_OPTS=""
ENV SERVER_PORT=8086
ENV SECURITY_ENABLED=true
ENV KEYCLOAK_ISSUER_URI="http://keycloak:8080/realms/jass"
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]



