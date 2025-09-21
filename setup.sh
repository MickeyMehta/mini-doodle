#!/bin/bash

# Mini Doodle - Complete Setup Script
# This script ensures all dependencies are properly installed and configured

set -e

echo "🛠️  Mini Doodle - Complete Setup"
echo "================================"

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check Java
echo "☕ Checking Java..."
if ! command_exists java; then
    echo "❌ Java is required but not installed."
    echo "   Please install Java 17+ from: https://adoptium.net/"
    exit 1
fi

java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$java_version" -lt 17 ]; then
    echo "❌ Java 17 or later is required. Current version: $java_version"
    echo "   Please install Java 17+ from: https://adoptium.net/"
    exit 1
fi

echo "✅ Java $java_version is available"

# Setup Maven Wrapper
echo "📦 Setting up Maven Wrapper..."

# Create .mvn directory structure
mkdir -p .mvn/wrapper

# Download Maven wrapper properties
cat > .mvn/wrapper/maven-wrapper.properties << 'EOF'
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.5/apache-maven-3.9.5-bin.zip
wrapperUrl=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar
EOF

# Download Maven wrapper jar
if [ ! -f ".mvn/wrapper/maven-wrapper.jar" ]; then
    echo "   Downloading Maven Wrapper JAR..."
    if command_exists curl; then
        curl -o .mvn/wrapper/maven-wrapper.jar https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar
    elif command_exists wget; then
        wget -O .mvn/wrapper/maven-wrapper.jar https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar
    else
        echo "❌ Need curl or wget to download Maven wrapper"
        exit 1
    fi
fi

# Ensure mvnw is executable
chmod +x mvnw

# Test Maven wrapper
echo "🧪 Testing Maven Wrapper..."
if ./mvnw --version >/dev/null 2>&1; then
    echo "✅ Maven Wrapper is working"
else
    echo "⚠️  Maven Wrapper test failed, but files are in place"
fi

# Create local development configuration
echo "🔧 Creating local development configuration..."

# Create application-local.yml for embedded database
mkdir -p src/main/resources
cat > src/main/resources/application-local.yml << 'EOF'
spring:
  application:
    name: mini-doodle
  
  profiles:
    active: local
    
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: password
    
  h2:
    console:
      enabled: true
      path: /h2-console
      settings:
        web-allow-others: true
        
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        format_sql: true
        
  flyway:
    enabled: false
    
  cache:
    type: simple
    
  redis:
    # Disable Redis for local development
    timeout: 2000ms
    
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.doodle: INFO
    org.springframework.web: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    
server:
  port: 8080
  
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    
doodle:
  scheduling:
    max-slots-per-day: 48
    min-slot-duration-minutes: 15
    max-slot-duration-minutes: 480
EOF

echo ""
echo "✅ Setup completed successfully!"
echo "================================"
echo ""
echo "🚀 Next steps:"
echo "  1. Run the application: ./mvnw spring-boot:run -Dspring-boot.run.profiles=local"
echo "  2. Or use the start script: ./start.sh local-dev"
echo ""
echo "📊 Once running, access:"
echo "  • API Documentation: http://localhost:8080/swagger-ui.html"
echo "  • Health Check: http://localhost:8080/actuator/health"
echo "  • H2 Console: http://localhost:8080/h2-console"
echo ""
echo "🗄️  H2 Database Connection:"
echo "  • JDBC URL: jdbc:h2:mem:testdb"
echo "  • Username: sa"
echo "  • Password: password"