# Banking API

[![CI](https://github.com/ryganzk/digital_banking_app/actions/workflows/ci.yml/badge.svg)](https://github.com/ryganzk/digital_banking_app/actions/workflows/ci.yml)

Portfolio Spring Boot REST API for a simple banking domain: JWT authentication, user-owned accounts, and money movement (deposit, withdraw, transfer) with an append-only transaction ledger.

**Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) (after starting the app)

## Stack

| Layer | Choice |
|--------|--------|
| Language | Java 21 |
| Framework | Spring Boot 4.1 |
| Security | Spring Security + JWT (jjwt) |
| Persistence | Spring Data JPA, PostgreSQL, Flyway |
| Validation | Jakarta Bean Validation |
| Tests | JUnit 5, Mockito, MockMvc, JaCoCo |

## Prerequisites

- JDK 21+
- Docker (for PostgreSQL)
- Maven Wrapper (`mvnw` / `mvnw.cmd`) — no global Maven install required

## Run locally

### 1. Start Postgres

```bash
docker compose up -d
```

This starts PostgreSQL on `localhost:5432` with:

- database: `banking_db`
- user / password: `banking` / `banking`

### 2. Start the API

```bash
# Windows
.\mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

App listens on [http://localhost:8080](http://localhost:8080).

### 3. Health check

```bash
curl.exe http://localhost:8080/health
```

Expected:

```json
{"status":"UP","service":"banking-api"}
```

### 4. Explore the API (Swagger)

Open [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html).

1. Call **register** or **login** under `/api/v1/auth`
2. Copy the `token` from the response
3. Click **Authorize**, paste the token (Swagger adds `Bearer `), then **Authorize**
4. Try protected account endpoints

OpenAPI JSON is also available at `/v3/api-docs`.

## API overview

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/health` | No | Liveness |
| `POST` | `/api/v1/auth/register` | No | Create user + JWT |
| `POST` | `/api/v1/auth/login` | No | Login + JWT |
| `POST` | `/api/v1/accounts` | Yes | Create account for current user |
| `GET` | `/api/v1/accounts` | Yes | List current user's accounts |
| `POST` | `/api/v1/accounts/{accountId}/deposit` | Yes | Deposit funds |
| `POST` | `/api/v1/accounts/{accountId}/withdraw` | Yes | Withdraw funds |
| `POST` | `/api/v1/accounts/{accountId}/transfer` | Yes | Transfer to another account number |

Protected routes require:

```http
Authorization: Bearer <token>
```

## Example flow

Use `curl.exe` on Windows (PowerShell’s `curl` alias is different).

### Register

```bash
curl.exe -s -X POST http://localhost:8080/api/v1/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"alex@example.com\",\"password\":\"password123\",\"firstName\":\"Alex\",\"middleName\":\"\",\"lastName\":\"Morgan\"}"
```

### Login

```bash
curl.exe -s -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"alex@example.com\",\"password\":\"password123\"}"
```

Copy the `token` from the response.

### Create an account

```bash
curl.exe -s -X POST http://localhost:8080/api/v1/accounts ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -d "{\"accountNumber\":\"ACC-001\",\"ownerName\":\"Alex Morgan\",\"initialBalance\":1000.00,\"currency\":\"USD\"}"
```

Note the returned account `id` (UUID).

### Deposit

```bash
curl.exe -s -X POST http://localhost:8080/api/v1/accounts/ACCOUNT_UUID/deposit ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -d "{\"amount\":100.00}"
```

### Transfer

Destination is looked up by **account number** (not UUID):

```bash
curl.exe -s -X POST http://localhost:8080/api/v1/accounts/SOURCE_ACCOUNT_UUID/transfer ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -d "{\"amount\":50.00,\"toAccountNumber\":\"ACC-002\"}"
```

Transfers write paired ledger rows (`TRANSFER_OUT` / `TRANSFER_IN`) linked by a shared `transferGroupId`.

## Errors

Failures return a consistent JSON body:

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Account not found",
  "timestamp": "2026-01-01T00:00:00Z"
}
```

Common codes: `NOT_FOUND` (404), `BAD_REQUEST` / `INSUFFICIENT_FUNDS` / `VALIDATION_ERROR` (400), `INVALID_CREDENTIALS` (401), `CONFLICT` (409).

## Tests & coverage

```bash
# Windows
.\mvnw.cmd test
.\mvnw.cmd verify

# macOS / Linux
./mvnw test
./mvnw verify
```

JaCoCo HTML report (after `verify`):

`target/site/jacoco/index.html`

## Project layout

```text
src/main/java/com/portfolio/banking_api/
  controller/     REST endpoints
  service/        Business logic (auth, accounts, JWT)
  domain/         JPA entities (User, Account, Transaction)
  repository/     Spring Data repositories
  dto/            Request/response records
  security/       JWT filter
  config/         Security configuration
  exception/      Typed exceptions + GlobalExceptionHandler
src/main/resources/
  db/migration/   Flyway SQL (users, accounts, transactions)
  application.yaml
```

## Configuration

Defaults live in `src/main/resources/application.yaml` (DB URL, JWT secret, token TTL). Change the JWT secret before any real deployment.

## License

Portfolio / personal project — no license specified.
