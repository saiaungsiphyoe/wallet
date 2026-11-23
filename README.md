# Wallet Application

A secure RESTful digital wallet application built with **Java 17 and Spring Boot**. This project demonstrates practical backend development practices, including JWT authentication, database migrations, wallet lifecycle management, financial transaction processing, concurrency control, and global exception handling.

Users can register, manage their wallets, activate wallets, deposit funds, withdraw funds, and transfer money between wallets while the application enforces ownership, wallet state transitions, and business rules.

## Features

### Authentication and Security
- JWT-based authentication
- Secure password hashing using BCrypt
- Stateless authentication with Spring Security
- Protected REST API endpoints
- Wallet ownership validation

### Wallet Management
- Automatic wallet creation upon user registration
- Wallet lifecycle management
- Wallet activation and status management
- Retrieve the authenticated user's wallet
- Retrieve wallet details by ID
- Validation of wallet ownership and state transitions

Supported wallet states:

- `PENDING`
- `ACTIVE`
- `SUSPENDED`
- `CLOSED`
- `REJECTED`

Supported wallet commands:

- `ACTIVATE`
- `SUSPEND`
- `CLOSE`
- `REJECT`

Only wallets in the `ACTIVE` state can perform financial transactions.

### Wallet Transactions
- Deposit funds
- Withdraw funds
- Transfer funds between wallets
- Wallet balance validation
- Currency validation
- Business rule validation before processing transactions
- Transaction history retrieval
- Optimistic locking to protect against conflicting concurrent updates

Supported transaction commands:

- `DEPOSIT`
- `WITHDRAW`
- `TRANSFER`

### Database and Error Handling
- MySQL database integration
- Version-controlled database migrations using Flyway
- Automatic schema migration on application startup
- Centralized exception handling
- Consistent JSON error responses
- Meaningful business validation messages

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language |
| Spring Boot | Application framework |
| Spring Security | Authentication and authorization |
| JWT | Stateless authentication |
| Spring Data JPA | Data persistence |
| Hibernate | Object-relational mapping |
| MySQL | Relational database |
| Flyway | Database schema migration |
| Maven | Build and dependency management |
| Docker | Application containerization |
| Docker Compose | Application and database orchestration |
| Springdoc OpenAPI | API documentation |
| MapStruct | DTO mapping |
| Lombok | Boilerplate code reduction |

## Architecture

The application follows a layered architecture that separates API handling, business logic, and data persistence.

```text
             Client
                |
                v
           Controller
                |
                v
             Service
                |
                v
           Repository
                |
                v
              MySQL
```

Additional components support the application:

- **DTOs:** Separate API request and response models from persistence entities.
- **Mappers:** Convert between entities and DTOs.
- **Security:** Handle authentication and authorization.
- **Exception Handling:** Centralize application and business exception responses.
- **Configuration:** Manage application, database, and security configuration.

## Wallet Lifecycle

Newly registered users receive a wallet in the `PENDING` state.

The wallet follows these lifecycle rules:

- `PENDING` wallets can be activated or rejected.
- `ACTIVE` wallets can be suspended or closed.
- `SUSPENDED` wallets can be reactivated or closed.
- `CLOSED` and `REJECTED` wallets cannot be reactivated.
- Only `ACTIVE` wallets can perform financial transactions.

```text
                 User Registration
                        |
                        v
                     PENDING
                    /       \
               ACTIVATE    REJECT
                  |           |
                  v           v
                ACTIVE     REJECTED
               /     \
          SUSPEND    CLOSE
             |          |
             v          v
          SUSPENDED   CLOSED
           /     \
      ACTIVATE   CLOSE
          |         |
          v         v
        ACTIVE    CLOSED
```

## REST API

All protected endpoints require a valid JWT access token, except for the public authentication endpoints and explicitly permitted application endpoints.

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Authenticate a user and receive a JWT |

### Wallet APIs

**Retrieve the current user's wallet**

```http
GET /api/wallets/me
```

Retrieves the authenticated user's wallet.

**Retrieve a wallet by ID**

```http
GET /api/wallets/{walletId}
```

Returns wallet information. Access is restricted to the wallet owner.

**Update wallet status**

```http
POST /api/wallets/{walletId}?command=ACTIVATE
```

Supported commands:

| Command | Description |
|---|---|
| `ACTIVATE` | Activate a pending or suspended wallet |
| `SUSPEND` | Suspend an active wallet |
| `CLOSE` | Permanently close an eligible wallet |
| `REJECT` | Reject a pending wallet |

The application validates each requested state transition before applying it.

### Wallet Transaction APIs

All transaction operations use:

```http
POST /api/wallet-transactions?command={COMMAND}
```

**Deposit**

```http
POST /api/wallet-transactions?command=DEPOSIT
```

Example request:

```json
{
  "toWalletId": 1,
  "amount": 1000
}
```

**Withdraw**

```http
POST /api/wallet-transactions?command=WITHDRAW
```

Example request:

```json
{
  "fromWalletId": 1,
  "amount": 500
}
```

**Transfer**

```http
POST /api/wallet-transactions?command=TRANSFER
```

Example request:

```json
{
  "fromWalletId": 1,
  "toWalletId": 2,
  "amount": 250
}
```

Transaction business rules include:

- The source wallet must be `ACTIVE`.
- The destination wallet must be `ACTIVE`.
- The source wallet must have sufficient funds for withdrawals and transfers.
- The transaction amount must be greater than zero.
- Referenced wallets must exist.
- Wallet currencies must be compatible.

### Transaction History

**Retrieve paginated transaction history**

```http
GET /api/wallets/{walletId}/transactions
```

**Retrieve an individual transaction**

```http
GET /api/wallets/{walletId}/transactions/{transactionId}
```

Transaction history includes transactions associated with the specified wallet, including incoming and outgoing transactions. Access is restricted to the authenticated wallet owner.

The transaction history endpoint supports pagination and sorting through Spring Data pagination parameters.

## Concurrency Control

The application uses **optimistic locking** to protect wallet balances against conflicting concurrent updates.

Each wallet maintains a version field. When multiple transactions attempt to update the same wallet concurrently, the version is checked to detect stale updates and prevent them from silently overwriting newer balances.

Conflicting updates are handled with an appropriate error response.

## Database Migrations

The project uses **Flyway** for version-controlled database schema migrations.

On application startup:

- Flyway validates the existing migration history.
- Pending migrations are executed automatically.
- The database schema is kept synchronized with the application.

Hibernate schema generation is disabled so that Flyway remains responsible for database schema changes.

## Global Exception Handling

The application centralizes exception handling to provide consistent and meaningful error responses.

Handled scenarios include:

- Invalid credentials
- Unauthorized access
- Access denied
- Request validation failures
- Wallet not found
- Wallet not active
- Invalid wallet state transitions
- Insufficient balance
- Invalid transaction requests
- Optimistic locking conflicts
- Unexpected application errors

Example error response:

```json
{
  "timestamp": "2026-07-25T13:29:16.726645773",
  "status": 400,
  "error": "Bad Request",
  "message": "Wallet is inactive.",
  "path": "/api/wallet-transactions"
}
```

## Getting Started

### Prerequisites

Choose one of the following setup options.

**Option 1: Docker Compose**
- Docker
- Docker Compose plugin

**Option 2: Run locally**
- Java 17 or later
- MySQL 8.0
- Maven (optional, since the project includes the Maven wrapper)

### Clone the Repository

```bash
git clone https://github.com/Aung-Si-Phyoe/wallet.git
cd wallet
```

### Run with Docker Compose

Docker Compose builds the application image and starts the application together with MySQL.

**1. Create your environment file**

Linux/macOS:

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

**2. Configure environment variables**

Edit `.env` and set your database passwords and JWT secret.

The values in `.env.example` are intended for local development only. Replace them with appropriate values before deploying the application.

**3. Build and start the services**

```bash
docker compose up --build
```

Docker Compose waits for the MySQL health check before starting the application.

**4. Access the application**

- API base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Health endpoint: `http://localhost:8080/actuator/health`

**5. Stop the services**

```bash
docker compose down
```

This preserves the named MySQL volume and its data.

To stop the services and remove the database volume as well:

```bash
docker compose down -v
```

**Note:** The `-v` option permanently removes the persisted database data.

### Run Locally

**1. Create a MySQL database**

```sql
CREATE DATABASE wallet;
```

**2. Configure the database connection**

Set the required environment variables.

Linux/macOS:

```bash
export DB_USERNAME=root
export DB_PASSWORD=myPassword
export JWT_SECRET_KEY=your-secret-key
export JWT_EXPIRATION=3600000
```

Windows PowerShell:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="myPassword"
$env:JWT_SECRET_KEY="your-secret-key"
$env:JWT_EXPIRATION="3600000"
```

If your MySQL connection differs from the default configuration, also set `DB_URL`.

**3. Start the application**

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On startup:

- Flyway automatically validates and applies pending database migrations.
- The application connects to the configured MySQL database.
- Protected endpoints require a valid JWT access token.

## Configuration

The application supports configuration through environment variables. The following defaults are defined for local development.

| Environment Variable | Default Value |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/wallet?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `mysql` |
| `JWT_SECRET_KEY` | Development secret defined in application configuration |
| `JWT_EXPIRATION` | `3600000` (1 hour) |

**Important:** Do not use the default database credentials or JWT secret in a production environment. Supply secure values through environment variables or an appropriate secrets management solution.

## API Authentication

Authenticate through the login endpoint to obtain a JWT access token.

Include the token in the `Authorization` header when calling protected endpoints:

```http
Authorization: Bearer <JWT_TOKEN>
```

Swagger UI also supports bearer-token authentication through its **Authorize** feature.

## Running Tests

Run the complete test suite and verify the application build using the Maven wrapper.

Linux/macOS:

```bash
./mvnw clean verify
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean verify
```

The integration test suite covers wallet lifecycle operations, financial transactions, transaction history, and concurrent wallet updates.

## Potential Future Improvements

- Refresh token support
- Email notifications
- Audit logging
- Wallet statements
- Multi-currency wallet support

## What This Project Demonstrates

This project showcases practical backend development skills, including:

- Spring Boot REST API development
- JWT authentication and Spring Security
- Secure API design
- Layered application architecture
- Business rule implementation
- Wallet lifecycle management
- Deposit, withdrawal, and transfer operations
- DTO and mapper patterns
- Flyway database migrations
- MySQL integration
- Global exception handling
- Transactional service methods
- Optimistic locking and concurrency control
- Docker-based application deployment
- Integration testing

---

**Built with Java, Spring Boot, and MySQL.**
