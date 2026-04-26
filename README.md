# Inventory & Order API Service

Spring Boot REST API for categories, products, and orders, backed by PostgreSQL, JPA, and Flyway.

## Tech Stack

- Java 21
- Spring Boot 3.5.12-SNAPSHOT
- Spring Web
- Spring Data JPA
- Spring Validation (`jakarta.validation`)
- Spring HATEOAS
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

## Hypermedia Responses

The API now uses Spring HATEOAS and returns HAL-style JSON for resource responses.

- Single-resource responses include `_links` such as `self`.
- Collection responses include `_embedded` plus collection-level `_links`.
- Product search returns a paged HAL response with page metadata under `page`.
- Validation and error responses remain plain JSON objects such as `{ "error": "..." }`.

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

Example response:

```json
{
  "_embedded": {
    "categoryResponseList": [
      {
        "id": 1,
        "name": "Electronics",
        "_links": {
          "self": { "href": "http://localhost:8080/categories/1" },
          "categories": { "href": "http://localhost:8080/categories" },
          "products": { "href": "http://localhost:8080/products/by-category/Electronics" }
        }
      }
    ]
  },
  "_links": {
    "self": { "href": "http://localhost:8080/categories" }
  }
}
```

#### `GET /categories/{id}`

```bash
curl http://localhost:8080/categories/1
```

#### `GET /categories/details`

```bash
curl "http://localhost:8080/categories/details?page=1&size=20"
```

#### `POST /categories`

```bash
curl -X POST http://localhost:8080/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Electronics"}'
```

#### `PUT /categories/{id}`

```bash
curl -X PUT http://localhost:8080/categories/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Audio"}'
```

#### `DELETE /categories/{id}`

```bash
curl -X DELETE http://localhost:8080/categories/1
```

### Products

#### `GET /products`

```bash
curl http://localhost:8080/products
```

#### `GET /products/{id}`

```bash
curl http://localhost:8080/products/1
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

#### `GET /products/search?name=&minPrice=&maxPrice=&page=&size=`

Pagination:
- `page` is **one-based** (`page=1` is the first page)
- defaults: `page=1`, `size=20`
- `size` must be between `1` and `100`
- default sort is `id,asc`

```bash
curl "http://localhost:8080/products/search?name=laptop&minPrice=1000&maxPrice=2000&page=1&size=20"
```

Example response:

```json
{
  "_embedded": {
    "productResponseList": [
      {
        "id": 1,
        "name": "Gaming Laptop",
        "description": "High-end laptop",
        "price": 1500.00,
        "categoryId": 1,
        "categoryName": "Electronics",
        "_links": {
          "self": { "href": "http://localhost:8080/products/1" },
          "products": { "href": "http://localhost:8080/products" },
          "category": { "href": "http://localhost:8080/categories/1" }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "http://localhost:8080/products/search?name=laptop&minPrice=1000&maxPrice=2000&page=1&size=20"
    }
  },
  "page": {
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "number": 0
  }
}
```

#### `GET /products/summaries`

```bash
curl http://localhost:8080/products/summaries
```

This endpoint returns concrete summary resources in HAL under `_embedded.productSummaryResponseList`.

Example response:

```json
{
  "_embedded": {
    "productSummaryResponseList": [
      {
        "name": "Monitor",
        "price": 299.99
      }
    ]
  },
  "_links": {
    "self": { "href": "http://localhost:8080/products/summaries" }
  }
}
```

#### `PUT /products/{id}`

Full replacement update (same required fields as `POST`).

```bash
curl -X PUT http://localhost:8080/products/2 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Keyboard Pro",
    "description": "Mechanical keyboard with RGB",
    "price": 99.99,
    "categoryId": 1
  }'
```

#### `PATCH /products/{id}`

Partial update for one or more fields (`name`, `description`, `price`, `categoryId`).

```bash
curl -X PATCH http://localhost:8080/products/2 \
  -H "Content-Type: application/json" \
  -d '{
    "price": 19.99
  }'
```

Notes:
- Empty payload `{}` returns `400`.
- Explicit `null` values are rejected (including `"categoryId": null`).
- Unknown fields return `400`.

#### `DELETE /products/{id}`

```bash
curl -X DELETE http://localhost:8080/products/1
```

### Orders

#### `GET /orders`

```bash
curl http://localhost:8080/orders
```

#### `GET /orders/{id}`

```bash
curl http://localhost:8080/orders/1
```

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

Example order response:

```json
{
  "id": 1,
  "customerName": "Alice",
  "status": "CREATED",
  "totalAmount": 250.00,
  "createdAt": "2026-04-26",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Keyboard",
      "quantity": 2,
      "unitPrice": 100.00,
      "lineTotal": 200.00
    }
  ],
  "_links": {
    "self": { "href": "http://localhost:8080/orders/1" },
    "orders": { "href": "http://localhost:8080/orders" },
    "items": { "href": "http://localhost:8080/orders/1/items" },
    "confirm": { "href": "http://localhost:8080/orders/1" },
    "cancel": { "href": "http://localhost:8080/orders/1" }
  }
}
```

For orders with status `CONFIRMED` or `CANCELLED`, the transition links `confirm` and `cancel` are omitted.

#### `PUT /orders/{id}`

Updates order `status` only.

```bash
curl -X PUT http://localhost:8080/orders/1 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED"
  }'
```

#### `DELETE /orders/{id}`

```bash
curl -X DELETE http://localhost:8080/orders/1
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
- `409 Conflict` for business conflicts (e.g. duplicate category or deleting referenced resources)
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

Example `400` for PATCH payload validation:

```bash
curl -X PATCH http://localhost:8080/products/2 \
  -H "Content-Type: application/json" \
  -d '{}'
```

```json
{
  "error": "At least one field must be provided"
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
