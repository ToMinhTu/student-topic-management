# build file JAR bang maven
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Cap quyen thuc thi cho maven
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Tai truoc cac dependency
RUN ./mvnw dependency:go-offline -B

# Copy source va packed file JAR, skip unit test
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Runtime application
FROM eclipse-temurin:21-jre
WORKDIR /app

# Tao user he thong khong co quyen root (--gid)
RUN groupadd --system app && useradd --system --gid app app

# Copy file .jar
COPY --from=builder --chown=app:app /app/target/*.jar app.jar

USER app
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]