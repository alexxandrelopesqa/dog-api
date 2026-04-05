FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn --batch-mode --no-transfer-progress -DskipTests dependency:go-offline

CMD ["mvn", "--batch-mode", "--no-transfer-progress", "clean", "test", "allure:report"]
