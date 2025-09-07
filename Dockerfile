# Stage 1: build
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw mvnw
COPY pom.xml pom.xml

RUN chmod +x mvnw && ./mvnw -DskipTests dependency:go-offline

COPY src src

RUN ./mvnw -DskipTests package

# Stage 2: runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/cloudstorage-1.0-SNAPSHOT.jar cloudstorage.jar

EXPOSE 8080

# явный контекст для Spring Boot
ENV SERVER_SERVLET_CONTEXT_PATH=/cloud

CMD ["java", "-jar", "cloudstorage.jar"]
