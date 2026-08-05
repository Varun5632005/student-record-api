# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "DRIVER=$(echo ${SPRING_DATASOURCE_URL:-} | grep -q 'postgresql' && echo 'org.postgresql.Driver' || echo 'org.h2.Driver') && java -Dserver.port=${PORT:-8081} -Dspring.datasource.driver-class-name=$DRIVER -jar app.jar"]
