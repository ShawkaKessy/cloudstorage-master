FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw mvnw
COPY pom.xml pom.xml

RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src src

RUN ./mvnw -q -DskipTests package

EXPOSE 8080

COPY target/cloudstorage-1.0-SNAPSHOT.jar cloudstorage.jar
CMD ["java", "-jar", "cloudstorage.jar"]
