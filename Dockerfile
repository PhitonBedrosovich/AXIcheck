# Multi-stage build for both services in one container

# Stage 1: Build Service1
FROM maven:3.9.5-eclipse-temurin-21 AS service1-builder
WORKDIR /build
COPY maven-settings.xml /root/.m2/settings.xml
COPY service1/pom.xml .
COPY service1/src ./src
RUN mvn clean package -DskipTests -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true

# Stage 2: Build Service2
FROM maven:3.9.5-eclipse-temurin-21 AS service2-builder
WORKDIR /build
COPY maven-settings.xml /root/.m2/settings.xml
COPY service2/pom.xml .
COPY service2/src ./src
RUN mvn clean package -DskipTests -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true

# Stage 3: Final runtime with both services
FROM eclipse-temurin:21-jre

# Install supervisor
RUN apt-get update && \
    apt-get install -y supervisor && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy both JAR files
COPY --from=service1-builder /build/target/service1-1.0-SNAPSHOT.jar /app/service1.jar
COPY --from=service2-builder /build/target/service2-1.0-SNAPSHOT.jar /app/service2.jar

# Create supervisor configuration
RUN mkdir -p /var/log/supervisor
COPY <<EOF /etc/supervisor/conf.d/services.conf
[supervisord]
nodaemon=true
logfile=/var/log/supervisor/supervisord.log
pidfile=/var/run/supervisord.pid

[program:service2]
command=java -jar /app/service2.jar
directory=/app
autostart=true
autorestart=true
startretries=3
startsecs=10
stderr_logfile=/var/log/supervisor/service2.err.log
stdout_logfile=/var/log/supervisor/service2.out.log
priority=100

[program:service1]
command=java -jar /app/service1.jar --spring.profiles.active=docker
directory=/app
autostart=true
autorestart=true
startretries=3
startsecs=15
stderr_logfile=/var/log/supervisor/service1.err.log
stdout_logfile=/var/log/supervisor/service1.out.log
priority=200
EOF

# Expose both ports
EXPOSE 8080 8081

# Start supervisor
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/services.conf"]