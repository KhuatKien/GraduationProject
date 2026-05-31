# Wayzy

A microservices platform built with Java \- Spring Boot and Maven. The repository contains multiple services (discovery, config, gateway, and domain services) and supporting modules.

## Repository layout

- Root modules and tooling:
  - `docker-compose.yml`, `DockerFile`, `env.example`, `mvnw`, `mvnw.cmd`
  - `setup-all-services-https.sh` (Bash script for Linux/WSL)
- Shared library:
  - `common-lib/`
- Infrastructure services:
  - `config/config-server/`
  - `discovery/discovery-server/`
  - `gateway/api-gateway/`
- Business services (each under `service/`):
  - `auth-service/`, `user-service/`, `booking-service/`, `campaign-service/`, `promotion-service/`, `tour-service/`, `notification-service/`, ...

## Prerequisites

- Windows 10/11
- JDK 17+
- Docker and Docker Compose (for running full stack)
- Git
- (Optional) WSL2 if you need to run `setup-all-services-https.sh`
