# Inventory & Order API Service

Spring Boot REST API for categories, products, and orders, backed by PostgreSQL, JPA, and Flyway.

## Tech Stack

- Java 21
- Spring Boot 3.5.12-SNAPSHOT
- Spring Web
- Spring Data JPA
- Spring Validation (`jakarta.validation`)
- Flyway (schema migrations)
- PostgreSQL
- Testcontainers (integration tests)
- Maven Wrapper (`./mvnw`)

## Architecture Overview

Layered structure:

- Controllers expose REST routes (`/categories`, `/products`, `/orders`).
- Services implement validation and business logic.
- Repositories handle persistence and query logic.
- DTOs separate request/response contracts from entities.

Main domain entities:

- `Category`
- `Product`
- `Order`
- `OrderItem`

## Getting Started

### Prerequisites

- Java 21
- Docker (for local PostgreSQL and integration tests)

### 1) Start PostgreSQL

```bash
docker compose up -d
```

### 2) Compile

```bash
./mvnw -q -DskipTests compile
```

### 3) Run the API

```bash
./mvnw spring-boot:run
```

API base URL: `http://localhost:8080`

## Database Notes

- PostgreSQL connection defaults are in `src/main/resources/application.properties`.
- Schema is managed by Flyway migrations in `src/main/resources/db/migration`.
- Hibernate DDL auto mode is `validate`.

## API Reference

### Categories

#### `GET /categories`

```bash
curl http://localhost:8080/categories
```

#### `POST /categories`

```bash
curl -X POST http://localhost:8080/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Electronics"}'
```

### Products

#### `GET /products`

```bash
curl http://localhost:8080/products
```

#### `POST /products`

`categoryId` is optional.

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Keyboard",
    "description": "Mechanical keyboard",
    "price": 89.99,
    "categoryId": 1
  }'
```

#### `GET /products/by-category/{categoryName}`

```bash
curl http://localhost:8080/products/by-category/Electronics
```

#### `GET /products/search?name=&minPrice=&maxPrice=`

```bash
curl "http://localhost:8080/products/search?name=laptop&minPrice=1000&maxPrice=2000"
```

#### `GET /products/summaries`

```bash
curl http://localhost:8080/products/summaries
```

### Orders

#### `POST /orders`

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Alice",
    "items": [
      {"productId": 1, "quantity": 2},
      {"productId": 2, "quantity": 1}
    ]
  }'
```

#### `GET /orders/{id}/items`

Returns order items for the given order id, including `productName`.

```bash
curl http://localhost:8080/orders/1/items
```

## Validation and Error Handling

Validation is enforced through bean validation and service-level checks.

Common error response shape:

```json
{
  "error": "..."
}
```

Typical status codes:

- `400 Bad Request` for validation errors
- `404 Not Found` for missing resources (e.g. missing product/order/category)
- `409 Conflict` for business conflicts (e.g. duplicate category)
- `500 Internal Server Error` for unexpected failures

Example `404`:

```bash
curl http://localhost:8080/orders/999999/items
```

```json
{
  "error": "Order not found: 999999"
}
```

## Testing

Run all tests:

```bash
./mvnw test
```

Notes:

- Integration tests use Testcontainers with PostgreSQL.
- Docker must be available when running integration tests.

## HTTP Request Collections

Ready-to-run request files:

- `src/httpreq/Category.http`
- `src/httpreq/Products.http`
- `src/httpreq/Order.http`

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
      db/migration/
  test/
    java/com/interview/demo/
  httpreq/
    Category.http
    Products.http
    Order.http
```

## Current Limitations

- No update/delete endpoints for categories, products, or orders.
- No pagination on product listing/search endpoints.
- Error payload is intentionally minimal (`{ "error": "..." }`).
