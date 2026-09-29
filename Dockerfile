FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY app/.mvn .mvn
COPY app/mvnw app/pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -B -DskipTests dependency:go-offline

COPY app/src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
RUN useradd --system --uid 1001 spring

COPY --from=build /workspace/target/*.jar app.jar
RUN mkdir -p logs && chown -R spring:spring /app

USER spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
