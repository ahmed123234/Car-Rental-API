# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-12-13

### Added
- Initial project setup with Spring Boot 3.2
- Entity models (User, Vehicle, Rental, Payment, Review)
- Security configuration with JWT
- Health checks and metrics (Actuator)
- Distributed tracing support (Micrometer + Zipkin)
- API documentation (Swagger/OpenAPI)
- Comprehensive testing framework (80%+ coverage)
- Docker support with docker-compose
- Logging configuration
- CORS and security headers

### Changed
- N/A

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- N/A

### Security
- JWT token-based authentication
- BCrypt password hashing
- Role-based access control

---

## [Unreleased]

### In Development
- Authentication endpoints (register, login, refresh)
- User profile management
- Vehicle management endpoints
- Rental booking system
- Payment processing
- Review system
