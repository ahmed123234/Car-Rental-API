FROM openjdk:17-slim-bullseye

WORKDIR /app

COPY target/car-rental-api-1.0.0.jar application.jar

RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD java -cp application.jar org.springframework.boot.loader.JarLauncher -c "curl -f http://localhost:8080/api/actuator/health || exit 1"

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["java", "-jar", "application.jar"]
