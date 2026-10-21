FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --grid app app
COPY --chown=app:app target/student-topic-management.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]