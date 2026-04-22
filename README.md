# Smart Campus API

A RESTful API built with **JAX-RS (Jersey 3.1.3)** and an embedded **Grizzly HTTP server** for managing campus rooms, sensors, and sensor readings. Built for the 5COSC022W Client-Server Architectures coursework at the University of Westminster.

All data is stored in-memory using `ConcurrentHashMap` — no database is used.

---

## Technology Stack

- Java 17
- JAX-RS (Jakarta RESTful Web Services)
- Jersey 3.1.3 (JAX-RS implementation)
- Grizzly HTTP Server (embedded)
- Jackson (JSON serialization)
- Maven (build tool)

---

## Project Structure

```
src/main/java/com/smartcampus/
├── Main.java                        → Starts the Grizzly server
├── SmartCampusApp.java              → JAX-RS Application class
├── model/
│   ├── Room.java                    → Room POJO
│   ├── Sensor.java                  → Sensor POJO
│   └── SensorReading.java           → SensorReading POJO
├── store/
│   └── DataStore.java               → Shared in-memory ConcurrentHashMap storage
├── resource/
│   ├── DiscoveryResource.java       → GET /api/v1
│   ├── RoomResource.java            → /api/v1/rooms
│   ├── SensorResource.java          → /api/v1/sensors
│   └── SensorReadingResource.java   → /api/v1/sensors/{id}/readings
├── exception/
│   ├── RoomNotEmptyException.java
│   ├── RoomNotEmptyExceptionMapper.java
│   ├── LinkedResourceNotFoundException.java
│   ├── LinkedResourceNotFoundExceptionMapper.java
│   ├── SensorUnavailableException.java
│   ├── SensorUnavailableExceptionMapper.java
│   └── GlobalExceptionMapper.java
└── filter/
    └── LoggingFilter.java           → Logs every request and response
```

---

## How to Build and Run

### Prerequisites
- Java 17 or higher installed
- IntelliJ IDEA (Community or Ultimate)
- Maven (built into IntelliJ — no separate install needed)

### Steps

**1. Clone the repository:**
```
git clone https://github.com/Umeshinduranga/smart-campus-api.git
```

**2. Open the project in IntelliJ IDEA:**
- File → Open → select the `smart-campus-api` folder
- IntelliJ will detect the `pom.xml` automatically
- Click **Load Maven Changes** if prompted and wait for dependencies to download

**3. Run the server:**
- In the left file tree, navigate to `src/main/java/com/smartcampus/Main.java`
- Right-click `Main.java` → Run 'Main.main()'
- Wait for the console to show:
```
Server started at http://localhost:8080/api/v1
Press Enter to stop...
```

**4. The API is now live at:**
```
http://localhost:8080/api/v1
```

**5. To stop the server:**
- Click the red square stop button in IntelliJ, or press Enter in the console

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1` | Discovery — returns API metadata and resource links |
| GET | `/api/v1/rooms` | Get all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room by ID |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors assigned) |
| GET | `/api/v1/sensors` | Get all sensors (supports `?type=` filter) |
| POST | `/api/v1/sensors` | Register a new sensor |
| GET | `/api/v1/sensors/{sensorId}` | Get a specific sensor by ID |
| GET | `/api/v1/sensors/{sensorId}/readings` | Get all readings for a sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a new reading to a sensor |

---

## Sample curl Commands

### 1. Discovery endpoint
```bash
curl http://localhost:8080/api/v1
```

### 2. Create a room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"R1\",\"name\":\"Lab A\",\"capacity\":30}"
```

### 3. Get all rooms
```bash
curl http://localhost:8080/api/v1/rooms
```

### 4. Create a sensor linked to room R1
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"S1\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"R1\"}"
```

### 5. Filter sensors by type
```bash
curl "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 6. Post a reading to sensor S1
```bash
curl -X POST http://localhost:8080/api/v1/sensors/S1/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":450.5}"
```

### 7. Get reading history for sensor S1
```bash
curl http://localhost:8080/api/v1/sensors/S1/readings
```

### 8. Delete a room
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/R1
```

---

## Error Responses

| Status Code | Scenario |
|-------------|----------|
| 400 Bad Request | Missing required field (e.g. room id is blank) |
| 403 Forbidden | Posting a reading to a sensor with MAINTENANCE status |
| 404 Not Found | Requested resource does not exist |
| 409 Conflict | Deleting a room that still has sensors assigned |
| 422 Unprocessable Entity | Creating a sensor with a roomId that does not exist |
| 500 Internal Server Error | Unexpected server error — returns clean JSON, no stack trace |

---

## Report - Question Answers

### Part 1.1 - JAX-RS Resource Lifecycle and Thread Safety

By default, JAX-RS creates a **new resource class instance for every incoming HTTP request** (request-scoped). This means instance variables on resource classes are never shared between requests — they are created fresh each time.

This has a direct impact on data management. If data was stored as instance variables inside a resource class, it would be lost after every single request. To safely share data across all requests, this project uses a `DataStore` class with `static` fields holding `ConcurrentHashMap` instances. Because JAX-RS can process multiple requests simultaneously on different threads, a standard `HashMap` would cause race conditions, two threads writing at the same time could corrupt data. `ConcurrentHashMap` is thread-safe by design, preventing data loss or corruption under concurrent access.

### Part 1.2 - HATEOAS and Hypermedia-Driven APIs

HATEOAS (Hypermedia as the Engine of Application State) is the principle that API responses should include links to related resources and available actions. The discovery endpoint at `GET /api/v1` returns a `resources` map containing URLs for the rooms and sensors collections.

This approach benefits client developers significantly. Instead of relying on separate static documentation that may go out of date, clients can navigate the entire API dynamically from a single entry point by following the links provided in responses. If URLs change in a future version, clients that follow hypermedia links rather than hardcoding paths are unaffected. This makes APIs more self-documenting, more resilient to change, and easier to explore.

### Part 2.1 - ID-only vs Full Object Returns

Returning only IDs in a list response is bandwidth-efficient but forces clients to make N additional requests to fetch the details of each item, this is known as the N+1 problem. For a campus system potentially managing thousands of rooms, this could result in thousands of extra HTTP calls.

Returning full objects uses more bandwidth per response but reduces round-trips. For large collections, the better approach is to return full objects with pagination so clients get complete data in a single request without downloading the entire dataset. For small filtered queries, full objects are the most practical choice.

### Part 2.2 - DELETE Idempotency

Yes, DELETE is idempotent in this implementation. The first call finds the room and deletes it, returning `204 No Content`. A second identical call finds no room and returns `404 Not Found`. The server state after both calls is identical, the room is gone either way. The HTTP response code differs between the first and subsequent calls, but the state of the resource does not change after the first deletion. This satisfies the HTTP specification's definition of idempotency: making the same request multiple times produces the same server state as making it once.

### Part 3.1 - @Consumes and Content-Type Mismatches

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that a method only accepts requests with a `Content-Type` of `application/json`. If a client sends a request with `Content-Type: text/plain` or `application/xml`, JAX-RS automatically returns **415 Unsupported Media Type** before the method is ever invoked. JAX-RS matches incoming requests to resource methods by comparing the request's `Content-Type` header against the `@Consumes` annotation. If no matching method is found, the request is rejected at the framework level, meaning no application code runs and no manual checking is needed.

### Part 3.2 - QueryParam vs PathParam for Filtering

Query parameters (`?type=CO2`) are the correct design choice for filtering collections. The resource being accessed is still the same collection, it is just being narrowed by a filter criterion. Using a path parameter (`/sensors/type/CO2`) would imply that `type/CO2` is itself a distinct named resource, which is semantically incorrect.

Query parameters also allow multiple filters to be combined easily, for example `?type=CO2&status=ACTIVE`, while path-based filtering makes combining multiple criteria extremely difficult and results in complex, hard-to-read URL structures. REST convention is clear: path parameters identify resources, query parameters filter or refine them.

### Part 4.1 - Sub-Resource Locator Pattern

The sub-resource locator pattern delegates the handling of nested URL paths to dedicated classes. In this project, `SensorResource` contains a locator method for `/{sensorId}/readings` that returns a `SensorReadingResource` instance. JAX-RS then inspects the annotations on the returned object to handle the actual request.

The architectural benefit is that each class has a single responsibility. `RoomResource` only manages rooms, `SensorResource` only manages sensors, and `SensorReadingResource` only manages readings. Without this pattern, all nested paths would be defined in one massive resource class, making it increasingly difficult to read, test, and maintain as the API grows. Separating concerns into dedicated classes makes the codebase modular, scalable, and much easier to extend with new features.

### Part 5.2 - Why 422 Instead of 404

A `404 Not Found` response means the requested URL does not correspond to any resource on the server. In this case, the URL `/api/v1/sensors` is perfectly valid, the problem is not the URL but the data inside the request body. The `roomId` field references a room that does not exist.

`422 Unprocessable Entity` is more semantically accurate because it means the request was syntactically valid JSON that was successfully parsed, but the data cannot be processed due to a logical or referential error. Using 422 tells the client precisely that their request body contains a broken reference, rather than misleading them into thinking the endpoint URL is wrong. This results in clearer error messages and faster debugging.

### Part 5.2 - Stack Trace Security Risks

Exposing internal Java stack traces to external API consumers carries serious security risks. A stack trace reveals:

- **Internal class and package names** - exposing the application's architecture and structure
- **Library names and exact version numbers** - allowing attackers to look up known CVEs for those specific versions
- **Internal file paths** - revealing server directory structure
- **Business logic flow** - showing exactly which code paths exist and where they fail

Attackers use this information to craft targeted exploits. For example, knowing the exact version of a library allows an attacker to search for published vulnerabilities affecting that version. A clean generic `500 Internal Server Error` JSON response gives away nothing about the internal implementation, significantly reducing the attack surface.

### Part 5.5 - Filters vs Manual Logging

Using a JAX-RS filter (`ContainerRequestFilter` and `ContainerResponseFilter`) for logging is far superior to manually inserting `Logger.info()` statements inside every resource method for several reasons.

A filter implements the cross-cutting concern once and it applies automatically to every single request and response across the entire API. Manual logging in every method risks being forgotten when new methods are added, creates code duplication, and mixes logging logic with business logic — violating the separation of concerns principle. Filters are also easier to maintain, can be enabled or disabled in one place, and guarantee consistent logging behaviour regardless of how many endpoints the API has. This is the standard industry approach to cross-cutting concerns such as logging, authentication, and CORS handling.

---

## GitHub Repository

[https://github.com/YOURUSERNAME/smart-campus-api](https://github.com/YOURUSERNAME/smart-campus-api)
