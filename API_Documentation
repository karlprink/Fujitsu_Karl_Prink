# API Reference

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
Triggered when an unrecognized value is provided for the parameters.

* **Code:** `400 Bad Request`
* **Content:**
  ```json
  {
  "timestamp": "2026-03-27T12:00:00.000000",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid city provided"
  }


#### 4. Internal Server Error
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
