# Product API Service

A Spring Boot REST API for managing products, backed by PostgreSQL and Spring Data JPA.
This project is intentionally compact and interview-friendly: the codebase demonstrates a clear layered architecture (`controller -> service -> repository`) with integration-style controller tests.

## Tech Stack

- Java 21
- Spring Boot 3.5.12-SNAPSHOT
- Spring Web (REST API)
- Spring Data JPA
- PostgreSQL
- Maven Wrapper (`./mvnw`)

## Architecture Overview

Request flow is organized by responsibility:

- `ProductController` exposes HTTP endpoints under `/products`.
- `ProductService` contains application logic and DTO-to-entity mapping.
- `ProductRepository` extends `JpaRepository<Product, Long>` for persistence.
- `Product` is the JPA entity mapped to the `products` table.

Data input for creation is modeled via `ProductDTO`:

```json
{
  "name": "string",
  "description": "string",
  "price": 19.99
}
```

## Getting Started

### Prerequisites

- Java 21
- Docker (for PostgreSQL via Compose)

### 1) Start PostgreSQL

```bash
docker compose up -d
```

### 2) Compile the project

```bash
./mvnw -q -DskipTests compile
```

### 3) Run the API

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

## Database Configuration Note

Current repository configuration uses:

- `compose.yaml`: `POSTGRES_DB=mydatabase`
- `application.properties`: `spring.datasource.url=jdbc:postgresql://localhost:5432/postgres`

To avoid environment-specific connection issues, align these values. Recommended option:

- keep Compose as-is and update datasource URL to:
  - `jdbc:postgresql://localhost:5432/mydatabase`

## API Reference

### Get all products

```http
GET /products
```

Example:

```bash
curl -X GET http://localhost:8080/products
```

### Create product

```http
POST /products
Content-Type: application/json
```

Example:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Keyboard",
    "description": "Mechanical keyboard",
    "price": 89.99
  }'
```

Example response:

```json
{
  "id": 1,
  "name": "Keyboard",
  "description": "Mechanical keyboard",
  "price": 89.99
}
```

## Testing

Run all tests:

```bash
./mvnw test
```

Current tests validate:

- Spring context startup
- `POST /products` creation flow
- `GET /products` endpoint availability

## Project Structure

```text
src/
  main/
    java/com/interview/demo/
      controller/
      service/
      repository/
      entity/
      dto/
    resources/
      application.properties
  test/
    java/com/interview/demo/
  httpreq/
    Products.http
```

## Known Limitations and Next Improvements

- Input validation is not yet enforced on request payloads.
- Update/delete endpoints are not exposed in the controller.
- Error response modeling is minimal (no standardized API error contract).
- Pagination/filtering is not implemented for product listing.
