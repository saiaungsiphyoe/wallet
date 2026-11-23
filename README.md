# Wallet Application

A secure RESTful digital wallet application built with **Spring Boot** that demonstrates modern backend development practices including **JWT authentication**, **Flyway database migrations**, **global exception handling**, and **wallet lifecycle management**.

The application allows users to register, manage their wallets, activate wallets, deposit funds, withdraw funds, and transfer money securely while enforcing wallet state transitions and business rules.

---

# Features

## Authentication & Security

* JWT-based authentication
* Secure password storage using BCrypt
* Stateless authentication
* Protected REST APIs using Spring Security

## Wallet Management

* Automatic wallet creation upon user registration
* Wallet lifecycle management
* Wallet activation and status management
* Retrieve authenticated user's wallets
* Retrieve wallet details by ID

Supported wallet states:

* Pending
* Active
* Deactivated
* Suspended
* Closed
* Rejected

Supported wallet commands:

* ACTIVATE
* DEACTIVATE
* SUSPEND
* CLOSE
* REJECT

## Wallet Transactions

* Deposit funds
* Withdraw funds
* Transfer funds between wallets
* Wallet balance validation
* Business rule validation before processing transactions

Supported transaction commands:

* DEPOSIT
* WITHDRAW
* TRANSFER

## Database

* MySQL
* Flyway database migrations
* Automatic schema versioning

## Error Handling

* Global exception handling
* Consistent JSON error responses
* Meaningful business validation messages

---

# Technology Stack

| Technology      | Purpose                        |
| --------------- | ------------------------------ |
| Java            | Programming Language           |
| Spring Boot     | Application Framework          |
| Spring Security | Authentication & Authorization |
| JWT             | Stateless Authentication       |
| Spring Data JPA | Data Persistence               |
| Hibernate       | ORM                            |
| MySQL           | Database                       |
| Flyway          | Database Migration             |
| Maven           | Build Tool                     |

---

# Architecture

The project follows a layered architecture:

```text
Controller
      │
      ▼
Service
      │
      ▼
Repository
      │
      ▼
MySQL
```

Additional layers:

* DTOs
* Entity Mappers
* Security
* Exception Handling
* Configuration

---

# Wallet Lifecycle

When a user registers, a wallet is automatically created with the **PENDING** status.

Only **ACTIVE** wallets can perform financial transactions.

```text
Register User
      │
      ▼
Create Wallet
      │
      ▼
PENDING
      │
Activate
      ▼
ACTIVE
      │
 ├── Deposit
 ├── Withdraw
 ├── Transfer
 │
 ├── Suspend
 ├── Deactivate
 └── Close
```

---

# REST API

## Authentication

| Method | Endpoint             | Description                             |
| ------ | -------------------- | --------------------------------------- |
| POST   | `/api/auth/register` | Register a new user                     |
| POST   | `/api/auth/login`    | Authenticate user and receive JWT token |

---

## Wallet APIs

### Retrieve Current User Wallets

```http
GET /api/wallets/me
```

Returns all wallets owned by the authenticated user.

---

### Retrieve Wallet by ID

```http
GET /api/wallets/{walletId}
```

Returns wallet information.

---

### Update Wallet Status

```http
POST /api/wallets/{walletId}?command=ACTIVATE
```

Supported commands:

| Command    | Description                  |
| ---------- | ---------------------------- |
| ACTIVATE   | Activate a pending wallet    |
| DEACTIVATE | Deactivate an active wallet  |
| SUSPEND    | Suspend wallet operations    |
| CLOSE      | Permanently close the wallet |
| REJECT     | Reject a pending wallet      |

---

## Wallet Transaction APIs

### Deposit

```http
POST /api/wallet-transactions?command=DEPOSIT
```

Example request body:

```json
{
  "toWalletId": 1,
  "amount": 1000
}
```

---

### Withdraw

```http
POST /api/wallet-transactions?command=WITHDRAW
```

Example request body:

```json
{
  "fromWalletId": 1,
  "amount": 500
}
```

---

### Transfer

```http
POST /api/wallet-transactions?command=TRANSFER
```

Example request body:

```json
{
  "fromWalletId": 1,
  "toWalletId": 2,
  "amount": 250
}
```

Business rules include:

* Source wallet must be ACTIVE
* Destination wallet must be ACTIVE
* Sufficient balance required
* Amount must be greater than zero
* Wallet must exist

---

# Database Migration

The project uses **Flyway** for version-controlled database migrations.

On application startup:

* Flyway validates migration history.
* Pending migrations are executed automatically.
* Database schema stays synchronized with the application.

---

# Global Exception Handling

The application centralizes exception handling to provide consistent and meaningful error responses.

Handled scenarios include:

* Invalid credentials
* Unauthorized access
* Validation failures
* Wallet not found
* Wallet not active
* Invalid wallet state transition
* Insufficient balance
* Invalid transaction request
* Resource not found

Example response:

```json
{
  "timestamp": "2026-07-25T13:29:16.726645773",
  "status": 400,
  "error": "Bad Request",
  "message": "Wallet is inactive.",
  "path": "/api/wallet-transactions"
}
```

---

# Getting Started

## Prerequisites

* Java 17+
* Maven
* MySQL

---

## Clone Repository

```bash
git clone https://github.com/Aung-Si-Phyoe/wallet.git
cd wallet
```

## Create the Database

Create a MySQL database named `wallet`.

```sql
CREATE DATABASE wallet;
```

---

## Configuration

The application is configured through **environment variables** with sensible default values.

If no environment variables are provided, the application will use the defaults shown below.

| Environment Variable | Default Value                                                                                    |
| -------------------- |--------------------------------------------------------------------------------------------------|
| `DB_URL`             | `jdbc:mysql://localhost:3306/wallet?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `DB_USERNAME`        | `root`                                                                                           |
| `DB_PASSWORD`        | `mysql`                                                                                          |
| `JWT_SECRET_KEY`     | Default development secret key                                                                   |
| `JWT_EXPIRATION`     | `3600000` (1 hour)                                                                               |

Example (Linux/macOS):

```bash
export DB_USERNAME=root
export DB_PASSWORD=myPassword
export JWT_SECRET_KEY=your-secret-key
export JWT_EXPIRATION=3600000
```

Example (Windows PowerShell):

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="myPassword"
$env:JWT_SECRET_KEY="your-secret-key"
$env:JWT_EXPIRATION="3600000"
```

> **Important:** Replace the default JWT secret with your own secure secret before deploying the application.


---

## Run the Application

### Linux / macOS

```bash
./mvnw spring-boot:run
```

### Windows

```cmd
mvnw.cmd spring-boot:run
```

On startup:

* Flyway automatically creates and updates the database schema.
* The application starts with the configured MySQL database.
* Protected endpoints require a valid JWT access token.

---

# Authentication

Authenticate using the login endpoint to obtain a JWT.

Include the token in every protected request.

```http
Authorization: Bearer <JWT_TOKEN>
```

---

# Potential Future Improvements

* Refresh tokens
* Email notifications
* Audit logs
* Wallet statements
* Multi-currency wallets

---

# What This Project Demonstrates

This project showcases practical backend development skills including:

* Spring Boot REST API development
* JWT authentication with Spring Security
* Secure API design
* Layered architecture
* Business rule implementation
* Wallet lifecycle management
* Deposit, withdraw, and transfer operations
* DTO and Mapper patterns
* Flyway database migrations
* MySQL integration
* Global exception handling
* Transactional service methods
* Clean, maintainable, and scalable backend design
