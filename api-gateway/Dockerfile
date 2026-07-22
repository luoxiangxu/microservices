# ============================================
# STAGE 1: Builder - Extract layers
# ============================================
FROM eclipse-temurin:17.0.4.1_1-jre AS builder
WORKDIR extracted
ADD target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# ============================================
# STAGE 2: Application
# ============================================
FROM eclipse-temurin:17.0.4.1_1-jre
WORKDIR application

# Copy layers from builder
COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./

# Expose port
EXPOSE 8080

# Entrypoint
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]