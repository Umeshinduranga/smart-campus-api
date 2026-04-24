# Smart Campus API

A RESTful API built with **JAX-RS (Jersey 3.1.3)** deployed on **Apache Tomcat** for managing campus rooms, sensors, and sensor readings. Built for the 5COSC022W Client-Server Architectures coursework at the University of Westminster.

All data is stored in-memory using `ConcurrentHashMap`, no database is used.

---

## Technology Stack

- Java 17
- JAX-RS (Java API for RESTful Web Services - javax namespace)
- Jersey 2.35 (JAX-RS implementation for Tomcat 9 compatibility)
- Apache Tomcat (web server)
- Jackson (JSON serialization)
- Maven (build tool)
- NetBeans 24 (IDE)

---

## Project Structure

```
src/main/java/com/smartcampus/
├── SmartCampusApp.java              → JAX-RS Application class (@ApplicationPath)
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

## Prerequisites

- Java JDK 17
- NetBeans 24
- Apache Tomcat 10 (configured inside NetBeans)
- Maven (built into NetBeans - no separate install needed)

---

## How to Build and Run

### Step 1 - Clone the repository

```
git clone https://github.com/Umeshinduranga/smart-campus-api.git
```

### Step 2 - Open in NetBeans

- Open NetBeans
- File → Open Project
- Select the `smart-campus-api` folder
- NetBeans will detect `pom.xml` automatically and download all dependencies

### Step 3 - Add Tomcat to NetBeans (if not already added)

- Go to **Tools → Servers → Add Server**
- Select **Apache Tomcat or TomEE**
- Browse to your Tomcat installation folder
- Set username: `admin` and password: `admin`
- Click Finish

### Step 4 - Set Tomcat as project server

- Right-click the project → **Properties → Run**
- Under Server select **Apache Tomcat**
- Context Path: `/`
- Click OK

### Step 5 - Clean and Build

- Top menu → **Run → Clean and Build Project**
- Wait for `BUILD SUCCESS` in the output panel

### Step 6 - Run

- Top menu → **Run → Run Project** or press **F6**
- Wait for `OK - Started application at context path [/]` in the output panel

### Step 7 - The API is now live at

```
http://localhost:8080/api/v1
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1` | Discovery  returns API metadata and resource links |
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
| 500 Internal Server Error | Unexpected server error, returns clean JSON, no stack trace |

---

## Report - Question Answers

### Part 1.1 - JAX-RS Resource Lifecycle and Thread Safety

By default, JAX-RS creates a new instance of a resource class for each HTTP request (i.e., Request Scoped) which prevents sharing of resource class variables between requests and ensures there is only one instance of a resource class variable created in response to each HTTP request. In order to maintain data that is shared across all HTTP requests, this project has implemented a DataStore class that has static fields that contain instances of ConcurrentHashMap. Since JAX-RS allows multiple requests to be processed simultaneously (multi-threading), using a regular HashMap could result in Race Conditions, where two threads are trying to update the same entry in the HashMap at the same time, thereby corrupting the underlying entry. Because ConcurrentHashMap implements thread-safe behavior, it guarantees that no data will be lost or become corrupt when accessed concurrently.

### Part 1.2 - HATEOAS and Hypermedia-Driven APIs

API responses using HATEOAS (Hypermedia as the Engine of Application State) send back links to associated resources or actions to allow access to those resources or perform an action. The discovery endpoint located at GET /api/v1 retrieves a resources map that lists the URLs of the rooms and sensors collections in the database. By providing this dynamic method of discovering the entire API from a single entry point via the links in the response to client applications, client application developers will benefit greatly from this method of discovering the API because they no longer need to rely on a static documentation that can quickly go out of date. If the URLs for an API change in a newer version, client applications that use the hypermedia links to traverse the API rather than hardcoding specific paths will not be affected, providing another self-documenting and robust feature to the API.

### Part 2.1 - ID-only vs Full Object Returns

Returning just IDs in a list response saves bandwidth but requires clients to make N more requests to get each item's details (this is called the N + 1 problem). For a campus system that may manage thousands of rooms, this could create thousands of extra HTTP calls. On the other hand, returning full objects results in more total bandwidth per response but reduces round trips. For larger collections, the best method is to return full objects with pagination so clients get all the data in a single request. For smaller filtered queries, returning full objects is also more practical.

### Part 2.2 - DELETE Idempotency

This implementation of the DELETE method is idempotent - meaning it will not alter the server state after repeated calls. The first time DELETE is called, the server finds the desired room, deletes the room from storage, and returns with a status code of 204 (No Content). The second time DELETE is called, there is no room to delete. The server will return 404 (Not Found) to indicate that there is no room to delete. After both calls, the server still does not have that room. While the HTTP response code differs between the two calls, the resource itself did not change state after the first call to DELETE. This is in line with the definition of idempotency provided by the specification of HTTP: subsequent calls of DELETE produce the same server state as the first call but return a different response code for each call.

### Part 3.1 - @Consumes and Content-Type Mismatches

The annotation @Consumes(MediaType.APPLICATION_JSON) specifies to JAX-RS that a method can only accept HTTP requests that contain a Content-Type header value of application/json. If a client sends an HTTP request with a Content-Type header of either text/plain or application/xml, JAX-RS will respond with a 415 Unsupported Media Type response code before invoking the method. JAX-RS performs the mapping between an incoming HTTP request to a resource method by matching the request's Content-Type header to the @Consumes annotation for that method. If the framework does not find any matching methods, it will reject the request thereby preventing any application code from executing and no manual validation will be performed.

### Part 3.2 - QueryParam vs PathParam for Filtering

Using Query Parameters (?type=CO2) is the proper way to filter our collection. We are accessing the same collection still, but are using a filter criteria to limit the data we are retrieving from the collection. Using Path Parameters (/sensors/type/CO2) would be saying that type/CO2 is a separate named resource and not the same resource as the collection accessing, thus, this would not be using the correct semantics. Query Parameters also allow for easy combination of multiple filters - e.g. ?type=CO2&status=ACTIVE, whereas Path Parameters make it more difficult to combine multiple criteria. The REST conventions are quite clear on this point; Path Parameters identify Resources, and Query Parameters filter or refine Resources.

### Part 4.1 - Sub-Resource Locator Pattern

The sub-resource Locator pattern delegates handling of nested URL path to the class. In the project, SensorResource has a locator method for /{sensorId}/readings that returns an instance of SensorReadingResource. The JAX-RS layer will inspect annotations on the object returned from the locator to actually handle a request. This architectural style requires each class to have a single responsibility: RoomResource manages rooms, SensorResource manages sensors, and SensorReadingResource manages readings. Without this pattern, all paths would be defined within a single large resource class and as the API grows would become ever more difficult to read, test, and maintain.

### Part 5.2 - Why 422 Instead of 404

When a request returns HTTP status code of 404, the requested URL points toward a resource that does not exist on the web server. For example, the URL /api/v1/sensors is indeed valid. However, the roomId field contained within the request body references a key corresponding to a room ID that does not exist. Hence, the returned status code 422 means the request body is syntactically valid JSON data, but could not be processed because of a logical reference error with the provided data. By using status code 422, it gives a clear indication to the client requesting the service that there is some "bad data" in their request body versus causing a client to misinterpret their request being invalid due to an invalid endpoint URL.

### Part 5.4 - Stack Trace Security Risks

There are major security implications with exposing application internal Java stack traces (and therefore any code or implementation details) via external APIs. Stack traces provide details about internal (application) class/package naming, providing insight into the application's architecture as well as the libraries used as well as their specific version numbers (allowing attackers access to publicly known CVEs associated with those versions), internal file paths which could expose a server's directory structure, as well as the business logic used within applications by providing detail about the code paths available and where failures occur. An attacker can use this information to exploit vulnerabilities in their applications. In contrast, a generic internal server error (i.e., 500) JSON response will not provide this level of detail about the implementation of the API functionality and will reduce the attack surface of the API dramatically.

### Part 5.5 - Filters vs Manual Logging

A JAX-RS logging filter is much better than putting log statements in all the resource methods. The filter addresses the cross-cutting concern of logging once and will apply to every request and response to the API automatically. If we were to implement, log each method individually, we would run the risk of forgetting to log, creating duplicate code, and mixing logging logic with business logic; this violates the separation of concerns principles. Maintenance is simpler with a filter because, turn it on or off in one place, and all endpoints have consistent logging.

---

## GitHub Repository

https://github.com/Umeshinduranga/smart-campus-api.git
