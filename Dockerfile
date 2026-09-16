FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S aft && adduser -S aft -G aft
COPY --from=build /src/target/*.jar app.jar
USER aft
EXPOSE 8092
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
