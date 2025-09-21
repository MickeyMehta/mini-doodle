#!/bin/bash

# Mini Doodle - Meeting Scheduling Platform
# Startup script for development and production

set -e

echo "🗓️  Mini Doodle - Meeting Scheduling Platform"
echo "============================================="

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check if Docker image exists
image_exists() {
    docker manifest inspect "$1" > /dev/null 2>&1
}

# Check dependencies
echo "📋 Checking dependencies..."

if ! command_exists docker; then
    echo "❌ Docker is required but not installed. Please install Docker first."
    exit 1
fi

if ! command_exists docker-compose; then
    echo "❌ Docker Compose is required but not installed. Please install Docker Compose first."
    exit 1
fi

# Detect platform architecture
ARCH=$(uname -m)
echo "🖥️  Detected architecture: $ARCH"

# Set Docker Compose command based on architecture
COMPOSE_CMD="docker-compose"
if [[ "$ARCH" == "arm64" ]] || [[ "$ARCH" == "aarch64" ]]; then
    export DOCKER_DEFAULT_PLATFORM=linux/arm64
    COMPOSE_CMD="docker-compose -f docker-compose.yml -f docker-compose.arm64.yml"
    echo "🍎 Using ARM64 optimized configuration"
fi

echo "✅ All dependencies are available"

# Make mvnw executable if it exists
if [ -f "./mvnw" ]; then
    chmod +x ./mvnw
fi

# Default mode
MODE=${1:-"full"}

case $MODE in
    "deps-only")
        echo "🚀 Starting dependencies only (PostgreSQL + Redis)..."
        $COMPOSE_CMD up -d postgres redis
        echo "✅ Dependencies started successfully!"
        echo "📊 Database: postgresql://localhost:5432/mini_doodle"
        echo "🗄️  Redis: localhost:6379"
        ;;
    
    "full")
        echo "🚀 Starting full application stack..."
        $COMPOSE_CMD up -d
        
        echo "⏳ Waiting for services to be ready..."
        sleep 30
        
        # Health check
        echo "🔍 Performing health check..."
        if curl -f -s http://localhost:8080/actuator/health > /dev/null; then
            echo "✅ Application is healthy!"
        else
            echo "⚠️  Application might still be starting up..."
        fi
        
        echo ""
        echo "🎉 Mini Doodle is running!"
        echo "================================"
        echo "📊 API Documentation: http://localhost:8080/swagger-ui.html"
        echo "🔗 API Base URL: http://localhost:8080/api/v1"
        echo "📈 Health Check: http://localhost:8080/actuator/health"
        echo "📊 Metrics: http://localhost:8080/actuator/metrics"
        echo "🔍 Prometheus: http://localhost:9090"
        echo ""
        echo "📚 Quick API Examples:"
        echo "  Create Calendar: POST /api/v1/calendars"
        echo "  Create Time Slot: POST /api/v1/calendars/{id}/slots"
        echo "  Schedule Meeting: POST /api/v1/meetings"
        ;;
    
    "logs")
        echo "📋 Showing application logs..."
        $COMPOSE_CMD logs -f app
        ;;
    
    "stop")
        echo "🛑 Stopping Mini Doodle..."
        $COMPOSE_CMD down
        echo "✅ Application stopped"
        ;;
    
    "clean")
        echo "🧹 Cleaning up (removing containers and volumes)..."
        $COMPOSE_CMD down -v --remove-orphans
        echo "✅ Cleanup completed"
        ;;
    
    "dev")
        echo "🔧 Starting development mode (dependencies only)..."
        $COMPOSE_CMD up -d postgres redis
        echo "✅ Dependencies started for development"
        echo ""
        echo "🚀 To run the application locally:"
        if command_exists mvn; then
            echo "  mvn spring-boot:run"
        else
            echo "  ./mvnw spring-boot:run"
        fi
        echo ""
        echo "🧪 To run tests:"
        if command_exists mvn; then
            echo "  mvn test"
        else
            echo "  ./mvnw test"
        fi
        ;;
    
    "docker-test")
        echo "🧪 Testing Docker image availability..."
        
        # Test different base images
        echo "Testing eclipse-temurin:17-jre-jammy..."
        if image_exists "eclipse-temurin:17-jre-jammy"; then
            echo "✅ eclipse-temurin:17-jre-jammy is available"
        else
            echo "❌ eclipse-temurin:17-jre-jammy not found"
        fi
        
        echo "Testing maven:3.9.5-eclipse-temurin-17..."
        if image_exists "maven:3.9.5-eclipse-temurin-17"; then
            echo "✅ maven:3.9.5-eclipse-temurin-17 is available"
            echo "💡 You can use Dockerfile.simple as fallback"
        else
            echo "❌ maven:3.9.5-eclipse-temurin-17 not found"
        fi
        
        echo ""
        echo "🔧 Available Dockerfile options:"
        echo "  Dockerfile         - Standard multi-stage build"
        echo "  Dockerfile.arm64   - ARM64 optimized"
        echo "  Dockerfile.simple  - Single-stage with Maven"
        echo "  Dockerfile.distroless - Lightweight distroless"
        ;;
    
    "local-dev")
        echo "🏠 Starting local development mode (no Docker required)..."
        chmod +x dev-setup.sh
        ./dev-setup.sh
        ;;
    
    "setup")
        echo "🛠️  Running complete setup..."
        chmod +x setup.sh
        ./setup.sh
        ;;
    
    *)
        echo "Usage: $0 [MODE]"
        echo ""
        echo "Modes:"
        echo "  setup       - Complete project setup (Maven, dependencies)"
        echo "  full        - Start complete application stack (default)"
        echo "  deps-only   - Start only dependencies (PostgreSQL + Redis)"
        echo "  dev         - Start dependencies for local development"
        echo "  local-dev   - Run with embedded database (no Docker)"
        echo "  docker-test - Test Docker image availability"
        echo "  logs        - Show application logs"
        echo "  stop        - Stop all services"
        echo "  clean       - Stop and remove all containers and volumes"
        echo ""
        echo "Examples:"
        echo "  $0              # Start full stack"
        echo "  $0 dev          # Start dependencies for development"
        echo "  $0 local-dev    # Run with embedded database"
        echo "  $0 logs         # View logs"
        echo "  $0 stop         # Stop application"
        exit 1
        ;;
esac