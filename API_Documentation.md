# API

**[Interactive API Documentation (Swagger)](https://editor.swagger.io/?url=https://raw.githubusercontent.com/karlprink/Fujitsu_Karl_Prink/main/openapi.yaml)**

This document describes the REST API endpoints available in the Delivery Fee Calculator application.

## Base URL
All API requests should be prefixed with the host and port where the application is running.
**Local development:** `http://localhost:8080`

---

## 1. Calculate Delivery Fee
Calculates the total delivery fee based on the specified city, vehicle type, and current weather conditions.

* **URL:** `/api/delivery-fee`
* **Method:** `GET`
* **Content-Type:** `application/json`

### Query Parameters

| Parameter | Type | Required | Description | Allowed Values |
| :--- | :--- | :--- | :--- | :--- |
| `city` | `String` | **Yes** | The city where the delivery takes place. | `Tallinn`, `Tartu`, `Pärnu` |
| `vehicleType` | `String` | **Yes** | The type of vehicle used for the delivery. | `Car`, `Scooter`, `Bike` |

### Success Response

* **Code:** `200 OK`
* **Content:**
  ```json
  {
    "totalFee": 3.5
  }

### Error Responses

#### 1. Forbidden Vehicle Type (Extreme Weather)
Triggered when the selected vehicle cannot be used due to severe weather conditions (e.g., trying to use a bike during a snowstorm).

* **Code:** `400 Bad Request`
* **Content:**
  ```json
  {
    "timestamp": "2026-03-27T12:00:00.000000",
    "status": 400,
    "error": "Bad Request",
    "message": "Usage of selected vehicle type is forbidden"
  }


#### 2. Missing Required Parameter
Triggered when city or vehicleType is missing from the request URL.

* **Code:** `400 Bad Request`
* **Content:**
  ```json
  {
  "timestamp": "2026-03-27T12:00:00.000000",
  "status": 400,
  "error": "Bad Request",
  "message": "Missing required parameter: vehicleType"
  }


#### 3. Invalid Argument (Unknown City or Vehicle)
Triggered when an unrecognized value is provided or no mapping exists.

* **Code:** `400 Bad Request`
* **Content:**
  ```json
  {
  "timestamp": "2026-03-27T12:00:00.000",
  "status": 400,
  "error": "Bad Request",
  "message": "Base fee rule not found for city: VÕRU and vehicle: CAR"
  }


#### 4. Malformed JSON Request
Triggered when the request body contains invalid JSON.

* **Code:** `400 Bad Request`
* **Content:**
  ```json
  {
  "timestamp": "2026-03-27T12:00:00.000",
  "status": 400,
  "error": "Bad Request",
  "message": "Malformed JSON request. Please check your request body format.",
  "expectedFormat": {
    "city": "String (e.g., 'TALLINN')",
    "vehicleType": "String (e.g., 'BIKE')",
    "fee": "Number (e.g., 3.5)"
  }
---

#### 5. Internal Server Error
Triggered when an unexpected error occurs on the server (e.g., database connection failure).

* **Code:** `500 Internal Server Error`
* **Content:**
  ```json
  {
  "timestamp": "2026-03-27T12:00:00.000000",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected internal server error occurred"
  }
---
---
## 2. Manage Base Fees (CRUD)

Provides a REST interface to dynamically manage the regional base fees used in the delivery fee calculations.

**Base URL:** `/api/rules/base-fees`
**Content-Type:** `application/json`

---

### 2.1 Get All Base Fees

Retrieves a list of all currently configured base fee rules.

**URL:** `/api/rules/base-fees`
**Method:** `GET`

**Success Response (200 OK):**

```json
[
  {
    "id": 1,
    "city": "TALLINN",
    "vehicleType": "CAR",
    "fee": 4.0
  },
  {
    "id": 2,
    "city": "TALLINN",
    "vehicleType": "SCOOTER",
    "fee": 3.5
  }
]
```

---

### 2.2 Create a New Base Fee

Adds a new pricing rule for a city and vehicle combination.

**URL:** `/api/rules/base-fees`
**Method:** `POST`

**Request Body:**

```json
{
  "city": "NARVA",
  "vehicleType": "BIKE",
  "fee": 2.5
}
```

**Success Response (200 OK):**
Returns the saved entity with its generated `id`.

**Error Response (400 Bad Request):** 
If a rule for the specific City + Vehicle combination already exists.
---

### 2.3 Update an Existing Base Fee

Modifies the fee value or details of an existing rule by its ID.

**URL:** `/api/rules/base-fees/{id}`
**Method:** `PUT`

**Request Body:**

```json
{
  "city": "TALLINN",
  "vehicleType": "BIKE",
  "fee": 4.0
}
```

**Success Response (200 OK):**
Returns the updated entity.

**Error Response (404 Not Found):**
If the rule with the specified `{id}` does not exist.

---

### 2.4 Delete a Base Fee

Removes a specific fee rule from the database.

**URL:** `/api/rules/base-fees/{id}`
**Method:** `DELETE`

**Success Response (200 OK):**
Empty body if the deletion was successful.

**Error Response (404 Not Found):**
If the rule with the specified `{id}` does not exist.

### 3. Manage City & Station Mappings

Endpoints for dynamically mapping delivery cities to specific National Weather Service observation stations.

**Base URL:** /api/cities
**Content-Type:** application/json

---

### 3.1 Get All City Mappings

Retrieves a list of all currently configured cities and their corresponding weather stations.

**Method:** `GET`
**Success Response (200 OK):**

```json
[
  {
    "id": 1,
    "city": "TALLINN",
    "stationName": "Tallinn-Harku"
  },
  {
    "id": 2,
    "city": "TARTU",
    "stationName": "Tartu-Tõravere"
  }
]
```
---

### 3.2 Add a New City Mapping

Maps a new city to a weather station.

**Note**: Successfully adding a new city mapping will automatically trigger an immediate background job to fetch the latest weather data for this specific station from the National Weather Service API.

**Method:** `POST`
**Request Body:**

```json
{
  "city": "KURESSAARE",
  "stationName": "Roomassaare"
}
```

**Success Response (200 OK):**

```json
{
  "id": 4,
  "city": "KURESSAARE",
  "stationName": "Roomassaare"
}
```

