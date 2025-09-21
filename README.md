# Mini Doodle - Meeting Scheduling Platform

A high-performance meeting scheduling platform built with Spring Boot that enables users to manage time slots, schedule meetings, and view calendar availability.

## Features

- **Time Slot Management**: Create, modify, and delete available time slots
- **Meeting Scheduling**: Convert available slots into meetings with participants
- **Calendar Management**: Personal calendars for each user
- **Availability Queries**: Query free/busy slots with aggregated views
- **High Performance**: Designed to handle hundreds of users with thousands of slots
- **RESTful APIs**: Complete REST API for all operations
- **Metrics & Monitoring**: Built-in metrics and health endpoints
- **Containerized**: Docker-compose setup for easy deployment

## Architecture

The application follows a layered architecture:
- **Domain Layer**: Core entities (Calendar, TimeSlot, Meeting)
- **Repository Layer**: JPA repositories with optimized queries
- **Service Layer**: Business logic and validation
- **Controller Layer**: REST API endpoints
- **Configuration**: Database, caching, and performance settings

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL
- **Caching**: Redis
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose
- **Testing**: JUnit 5, Testcontainers
- **Documentation**: OpenAPI/Swagger
- **Metrics**: Micrometer with Prometheus

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 17+ (for local development)

### Running with Docker Compose

```bash
# Clone and navigate to the project
cd mini-doodle

# Start all services (auto-detects platform)
./start.sh full

# Alternative: Manual docker-compose
docker-compose up -d

# Check service health
curl http://localhost:8080/actuator/health
```

#### Docker Troubleshooting

If you encounter Docker image issues (especially on Apple Silicon):

```bash
# Test Docker image availability
./start.sh docker-test

# Use development mode as fallback
./start.sh dev
./mvnw spring-boot:run

# Force platform (if needed)
export DOCKER_DEFAULT_PLATFORM=linux/arm64
./start.sh full
```

See `DOCKER_TROUBLESHOOTING.md` for detailed solutions.

The application will be available at:
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Metrics**: http://localhost:8080/actuator/metrics

## API Documentation

### Calendar Management

#### Create Calendar
```http
POST /api/v1/calendars
Content-Type: application/json

{
  "name": "John's Calendar",
  "userId": "user123",
  "timezone": "UTC"
}
```

#### Get Calendar
```http
GET /api/v1/calendars/{calendarId}
```

### Time Slot Management

#### Create Time Slot
```http
POST /api/v1/calendars/{calendarId}/slots
Content-Type: application/json

{
  "startTime": "2025-01-20T10:00:00Z",
  "endTime": "2025-01-20T11:00:00Z",
  "status": "AVAILABLE"
}
```

#### Update Time Slot
```http
PUT /api/v1/calendars/{calendarId}/slots/{slotId}
Content-Type: application/json

{
  "startTime": "2025-01-20T10:00:00Z",
  "endTime": "2025-01-20T11:30:00Z",
  "status": "BUSY"
}
```

#### Delete Time Slot
```http
DELETE /api/v1/calendars/{calendarId}/slots/{slotId}
```

#### Query Available Slots
```http
GET /api/v1/calendars/{calendarId}/slots/available?startDate=2025-01-20&endDate=2025-01-27
```

### Meeting Management

#### Schedule Meeting
```http
POST /api/v1/meetings
Content-Type: application/json

{
  "slotId": "slot123",
  "title": "Project Review",
  "description": "Weekly project review meeting",
  "participants": ["user123", "user456"]
}
```

#### Get Meeting
```http
GET /api/v1/meetings/{meetingId}
```

#### Update Meeting
```http
PUT /api/v1/meetings/{meetingId}
Content-Type: application/json

{
  "title": "Updated Project Review",
  "description": "Updated description",
  "participants": ["user123", "user456", "user789"]
}
```

## Performance Considerations

- **Database Indexing**: Optimized indexes on frequently queried columns
- **Caching**: Redis caching for frequently accessed data
- **Connection Pooling**: HikariCP for efficient database connections
- **Batch Operations**: Support for bulk operations
- **Pagination**: All list endpoints support pagination
- **Async Processing**: Background tasks for heavy operations

## Development

### Local Development Setup

```bash
# Start dependencies
docker-compose up -d postgres redis

# Run the application
./mvnw spring-boot:run
```

### Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

## Monitoring

### Health Checks
- **Application Health**: `/actuator/health`
- **Database Health**: `/actuator/health/db`
- **Cache Health**: `/actuator/health/redis`

### Metrics
- **Custom Metrics**: Meeting scheduling rates, slot utilization
- **JVM Metrics**: Memory, GC, thread pools
- **Database Metrics**: Connection pool, query performance

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.