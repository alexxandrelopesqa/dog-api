FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw mvnw
COPY src ./src
COPY pom.xml .

RUN mvn --batch-mode --no-transfer-progress -DskipTests dependency:go-offline

RUN chmod +x mvnw

CMD ["./mvnw", "--batch-mode", "--no-transfer-progress", "clean", "test", "allure:report"]
