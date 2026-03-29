### Karl Prink

# Delivery Fee Calculator (Fujitsu Trial Task/Fujitsu proovitöö)

![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)

A robust Spring Boot REST application that calculates delivery fees for different vehicle types based on regional base fees and real-time weather conditions in Estonia.

## Features

* **Real-time Weather Integration:** Periodically fetches weather data from the Estonian Environment Agency XML API.
* **Smart Startup Synchronization:** Calculates dynamic cron intervals to prevent unnecessary API spamming on application restarts. It only fetches new data if the database is empty or the latest entry is older than the configured cron interval.
* **RESTful API:** Provides a clean endpoint (`/api/delivery-fee`) for fee calculations.
* **Global Exception Handling:** Uses `@ControllerAdvice` to gracefully catch business logic errors (e.g., forbidden vehicle usage due to extreme weather) and bad requests, returning standardized and readable JSON responses instead of stack traces.
* **Containerized:** Fully Dockerized with a multi-stage build process for lightweight and efficient deployment, including persistent volume mapping for the H2 database.
* **High Test Coverage:** Comprehensive unit and WebMvc tests using JUnit 5 and modern Mockito practices (`@MockitoBean`).
* **Historical Weather Data Support:** Delivery fees can be calculated for past dates using the timestamp parameter.
* **Dynamic Fee Management (CRUD):** Base fees are not hardcoded. They are stored in an H2 database and can be managed dynamically via a dedicated REST API without restarting the application.
* **Interactive API Docs (Swagger):** Built-in Swagger UI for testing and exploring endpoints directly from the browser.

## Tech Stack

* **Java 21**
* **Spring Boot 3.4.x** (Web, Data JPA, Validation)
* **H2 Database** (File-based for persistence across container restarts)
* **Docker & Docker Compose**
* **JUnit 5 & Mockito** (Testing)

## Running the Application

### Using Docker

1. Make sure Docker is running on your machine.
2. Open a terminal in the project root directory and run:
   ```bash
   docker-compose up --build
3. The application will be available at http://localhost:8080

---
# API Summary

## 1. Calculate Delivery Fee

**GET** `/api/delivery-fee`

### Parameters

- **city** (String, required): Target city (`Tallinn`, `Tartu`, or `Pärnu`)
- **vehicleType** (String, required): Vehicle used for delivery (`Car`, `Scooter`, or `Bike`)

### Example Request

  ```http
  GET http://localhost:8080/api/delivery-fee?city=Tallinn&vehicleType=Scooter
```

### Example Successful Response (200 OK)

* **Content:**
  ```json
  {
    "totalFee": 3.5
  }

## 2. Manage Base Fees (CRUD)

The application seeds default base fees on startup, but they can be modified on the fly.

### List all base fees

```
GET /api/rules/base-fees
```

### Add new fee rule

```
POST /api/rules/base-fees
```

### Update existing rule

```
PUT /api/rules/base-fees/{id}
```

### Delete fee rule

```
DELETE /api/rules/base-fees/{id}
```

### Example Request (Update Tallinna Bike base fee to 4.0€)

```bash
curl -X PUT "http://localhost:8080/api/rules/base-fees/3" \
     -H "Content-Type: application/json" \
     -d '{
          "city": "TALLINN",
          "vehicleType": "BIKE",
          "fee": 4.0
         }'
      ```
```

## 3.Dynamic City & Station Management

**POST** /api/cities
Map a new city to an official weather station. Adding a new mapping triggers an immediate weather import for that station.

### Example Error Response (400 Bad Request)
(Triggered if weather conditions are too extreme for the selected vehicle)

* **Content:**
  ```json
  {
  "timestamp": "2026-03-27T12:00:00.000000",
  "status": 400,
  "error": "Bad Request",
  "message": "Usage of selected vehicle type is forbidden"
  }

### [More About API](API_Documentation.md)

# Database Access (H2 Console)

You can inspect the saved weather data directly through the web browser.

Go to:

```
http://localhost:8080/h2-console
```

Ensure the login details match your running environment.

## If running via Docker

- **Driver Class:** `org.h2.Driver`
- **JDBC URL:** `jdbc:h2:file:/app/data/weatherdb`
- **User Name:** `sa`
- **Password:** *(leave blank)*

Click **Connect** and run:

```sql
SELECT * FROM WEATHER_DATA;
```

to view the fetched weather conditions.

---

# Key Architectural Decisions

## API Prefixing (`/api/...`)

The REST endpoint is prefixed with `/api` to allow future scalability (e.g., hosting a frontend on the root path) and easy reverse-proxy configurations.

## Duplicate Prevention

The scheduled CronJob utilizes a `SELECT EXISTS` database query to ensure that exact duplicate weather records (same station and timestamp) are discarded, saving storage and ID sequences.

## Multi-Stage Dockerfile

The Dockerfile uses the `maven:eclipse-temurin-21` image just for building the `.jar`, and a much smaller `eclipse-temurin:21-jre-alpine` image for running it, keeping the final container size minimal.

## Strategy Pattern

Delivery fee surcharges (Wind, Temperature, Phenomenon) are implemented as separate strategies, making the system easy to extend without modifying the main service.

## Global Exception Handling

By using @RestControllerAdvice, we ensure that even technical errors return a user-friendly JSON instead of a generic error page.

## Layered Architecture

The application follows a strict Layered Architecture pattern to ensure a clean Separation of Concerns (SOC). This makes the codebase modular, easy to test, and maintainable.

**Web/Controller Layer:** Handles incoming HTTP requests, performs basic input validation, and maps business results to standardized JSON responses using GlobalExceptionHandler.

**Service/Business Layer:** The core of the application. It contains the business logic for fee calculations, weather data processing, and rule validation. It remains decoupled from the specific database or transport protocols.

**Data Access/Repository Layer:** Uses Spring Data JPA to interact with the H2 database. It abstracts complex SQL queries into simple, reusable method signatures.

**Infrastructure/External Layer:** Manages communication with the Estonian Environment Agency XML API via a dedicated RestClient, ensuring that external API changes don't leak into the core business logic.
---
