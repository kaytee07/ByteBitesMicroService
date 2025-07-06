# ByteBites Cloud-Native Microservices System

## 🚀 Contextual Overview

This project details the architecture and implementation of a cloud-native microservices system for **ByteBites**, an online food delivery startup. Building upon prior experiences with monolithic applications, this system is designed for **rapid growth**, emphasizing **modularity, security, and resilience**. The architecture leverages **Spring Boot**, **Spring Cloud**, **JWT** for token-based security, and **OAuth2** for external authentication, with **event-driven communication** facilitated by **Kafka/RabbitMQ**.

### Scenario: The ByteBites Platform

ByteBites connects **customers** with **local restaurants**. The platform's core requirements include:

* **Scalable Microservices**: Dedicated services for authentication, restaurants, orders, and notifications.
* **Centralized Configuration & Service Discovery**: Utilizing Spring Cloud Config and Eureka.
* **Role-Based Access Control (RBAC)**: Defined roles include `ROLE_CUSTOMER`, `ROLE_RESTAURANT_OWNER`, and `ROLE_ADMIN`.
* **JWT-based Security**: Centrally managed by the `auth-service`.
* **Event-Driven Communication**: Asynchronous processing via Kafka/RabbitMQ.
* **Circuit Breaker**: Implemented with Resilience4j for enhanced service resilience.

## 🧱 Architecture Overview

### 🔗 Core Services

| Service             | Description                                          |
| :------------------ | :--------------------------------------------------- |
| `discovery-server`  | Eureka registry for service discovery                |
| `config-server`     | Centralized Spring Cloud Config server               |
| `api-gateway`       | Routes all traffic and handles security filters      |
| `auth-service`      | Handles registration, login, JWT issuance            |
| `restaurant-service` | Manages restaurant data and menus                    |
| `order-service`     | Manages customer orders                              |
| `notification-service` | Sends email/push notifications after order placement |

### 🔐 Authentication & Authorization Flow

1.  **Login via Auth-Service**:
   * `POST /auth/login` returns a JWT if email/password is valid.
   * OAuth2 login (e.g., Google, GitHub) returns a JWT with `ROLE_CUSTOMER` by default.
   * The generated JWT is stored client-side and used for subsequent requests.

2.  **API Gateway**:
   * Validates the JWT for all incoming requests.
   * Forwards valid claims (e.g., user ID, roles) to downstream services via HTTP headers.

3.  **Role-Based Access Control (RBAC)**:
   * Enforced using Spring Security's `@PreAuthorize` annotations within the `restaurant-service` and `order-service`.
   * **Examples**:
      * Only `ROLE_CUSTOMER` can place orders.
      * `ROLE_RESTAURANT_OWNER` can update only their own restaurant data.
      * `ROLE_ADMIN` can access user management endpoints.

4.  **Resource Ownership**:
   * `order-service` and `restaurant-service` validate resource ownership by extracting the user ID from the JWT claims forwarded by the API Gateway, ensuring users can only access/modify their own data or resources they are authorized for.

## ⚙️ System Features Implemented

### ✅ Part 1: Core Infrastructure Services

* **`discovery-server`**: Implemented using Eureka, providing a robust service registry without additional security layers (as it's internal).
* **`config-server`**: Configured to serve application configurations from an external Git repository, enabling centralized and version-controlled management of service properties.
* **`api-gateway`**: Routes all incoming traffic to the appropriate microservices. Crucially, it integrates a JWT validation filter to secure all endpoints before forwarding requests.

### ✅ Part 2: Authentication & User Management

* **`auth-service`**:
   * Handles user registration with passwords securely hashed using BCrypt.
   * Manages JWT creation, embedding user roles and expiration times.
   * Assigns `ROLE_CUSTOMER` by default to newly registered external users.
   * (Optional) Includes a refresh token endpoint for extended session management.

### ✅ Part 3: Business Microservices

* **🥘 `restaurant-service`**:
   * Provides CRUD (Create, Read, Update, Delete) operations for restaurant and menu items.
   * **Security**: Only `ROLE_RESTAURANT_OWNER` can manage their own restaurant data. All authenticated users (`ROLE_CUSTOMER`, `ROLE_RESTAURANT_OWNER`, `ROLE_ADMIN`) can view restaurants and menus.

* **🛒 `order-service`**:
   * Enables customers to create and view food orders.
   * Orders progress through various statuses: `PENDING`, `CONFIRMED`, `DELIVERED`, etc.
   * **Security**: Only `ROLE_CUSTOMER` can place orders. Customers can only view their own placed orders. Restaurant owners can view orders placed for their specific restaurants.

### ✅ Part 4: Event-Driven Messaging (Kafka/RabbitMQ)

* **`notification-service`**: Listens to `OrderPlacedEvent` published by the `order-service`. It simulates sending email/push notifications to relevant parties (e.g., customer, restaurant).
* **`restaurant-service`**: Also listens to `OrderPlacedEvent`. Upon receiving this event, it can initiate internal processes for order preparation.
* **`order-service`**: Publishes an `OrderPlacedEvent` to the message broker immediately after successfully saving a new order, ensuring loose coupling and asynchronous processing.

### ✅ Part 5: Resilience with Resilience4j

* **Circuit Breaker**: Applied to all outgoing service-to-service calls (e.g., `order-service` calling `restaurant-service`). This protects the system from cascading failures.
* **Fallback Methods**: Implemented for circuit-breaker enabled calls, providing custom messages or default responses when a dependency is unavailable or failing.
* **Retry & Timeout**: (Optional) Configurable retry mechanisms and timeouts for service calls to enhance robustness.

### 🧪 Example Circuit Breaker + Actuator in Action

To observe the circuit breaker in action:

1.  Ensure all services are running initially.
2.  Gracefully shut down the `restaurant-service`.
3.  As a `ROLE_CUSTOMER`, attempt to place an order via the `order-service`.
4.  The `order-service` will trigger its configured fallback method because `restaurant-service` is unavailable.
5.  Access the `/actuator/circuitbreakerevents` endpoint on the `order-service` (e.g., `http://localhost:8082/actuator/circuitbreakerevents`).
6.  You should observe the circuit breaker status transitioning to `OPEN` and corresponding fallback logs.

## 🔐 Security Features Summary

| Feature                 | Description                                                                 |
| :---------------------- | :-------------------------------------------------------------------------- |
| **JWT-based Auth** | `auth-service` issues and validates JWTs for secure communication.          |
| **Gateway Auth Filter** | `api-gateway` validates JWTs and adds user information (ID, roles) to headers before forwarding. |
| **RBAC** | Enforced via `@PreAuthorize` annotations and granular service-level logic.  |
| **OAuth2 Login** | Supports Google/GitHub OAuth2 login, mapping external users to roles.       |
| **Stateless Sessions** | Leverages token-based security, eliminating the need for server-side sessions. |
| **Resource Ownership Checks** | Services validate that users can access or modify only their own data or authorized resources. |
| **Service Communication** | All internal services trust the API Gateway and rely on headers for user context. |
| **RabbitMQ Messaging** | Decouples processing between services, enhancing system resilience.         |
| **Circuit Breaker** | Resilience4j used to protect services from failures in downstream dependencies. |

## 🔍 Sample API Endpoints

| Endpoint                      | Method | Role Required           | Description                                             |
| :---------------------------- | :----- | :---------------------- | :------------------------------------------------------ |
| `/auth/register`              | `POST` | Public                  | Registers a new user account.                           |
| `/auth/login`                 | `POST` | Public                  | Authenticates user and returns JWT.                     |
| `/api/restaurants`            | `GET`  | Authenticated           | Retrieves a list of all restaurants.                    |
| `/api/restaurants`            | `POST` | `ROLE_RESTAURANT_OWNER` | Creates a new restaurant (by owner).                    |
| `/api/orders`                 | `POST` | `ROLE_CUSTOMER`         | Places a new food order.                                |
| `/api/orders/{id}`            | `GET`  | Resource owner only     | Retrieves details of a specific order.                  |
| `/admin/users`                | `GET`  | `ROLE_ADMIN`            | Retrieves a list of all users (admin function).         |

## 📦 Deliverable

This project is structured as follows:

* **GitHub Monorepo**: A single repository containing all microservices under a unified folder structure (e.g., `services/auth-service`, `services/restaurant-service`, etc.).
* **GitHub Config Repo**: A separate, dedicated GitHub repository storing all Spring Cloud Config files for the application.

## ⚙️ Setup Instructions

To get the ByteBites microservices system up and running, follow these steps:

1.  **Clone Repositories**:
   * Clone the main monorepo containing all service code:
       ```bash
       git clone <your-monorepo-url>
       cd bytebites-monorepo
       ```
   * Clone the Spring Cloud Config repository:
       ```bash
       git clone <your-config-repo-url>
       ```
     Ensure your `config-server` is configured to point to this repository.

2.  **Prerequisites**:
   * **Java 17+**: Ensure you have a compatible Java Development Kit (JDK) installed.
   * **Apache Maven**: For building and managing dependencies.
   * **Docker / Docker Compose**: Recommended for easily setting up Kafka/RabbitMQ and other external dependencies.

3.  **External Dependencies Setup**:
   * **Message Broker (Kafka/RabbitMQ)**:
      * If using Docker, you can start a broker instance.
         * **For Kafka**: `docker-compose -f docker-kafka.yml up -d` (assuming you have a `docker-kafka.yml` file with Kafka and Zookeeper setup).
         * **For RabbitMQ**: `docker-compose -f docker-rabbitmq.yml up -d` (assuming you have a `docker-rabbitmq.yml` file).
      * Ensure your services are configured to connect to the correct broker addresses.

4.  **Build Services**:
   * Navigate to the monorepo root directory.
   * Build all services using Maven:
       ```bash
       mvn clean install -DskipTests
       ```

5.  **Configure Application Properties**:
   * Verify that `application.yml` or `bootstrap.yml` files in each service correctly point to the `config-server` and the message broker. The `config-server` itself must point to your Git config repository.

## 🚀 How to Test Each Flow

### 1. Authentication & User Management

* **User Registration**:
   * Send a `POST` request to `/auth/register` with user credentials (email, password).
   * Verify a new user is created in the database (or via admin endpoint if available).
* **User Login (JWT)**:
   * Send a `POST` request to `/auth/login` with registered credentials.
   * Expect a JWT in the response body. Save this token.
* **OAuth2 Login**:
   * Access the OAuth2 login endpoint (e.g., `/oauth2/authorization/google`).
   * Complete the OAuth2 flow; the `auth-service` should redirect with a JWT.

### 2. Restaurant Management

* **View Restaurants (Authenticated)**:
   * Use the JWT obtained from login.
   * Send a `GET` request to `/api/restaurants` with `Authorization: Bearer <JWT>`.
   * Verify that all restaurants are returned.
* **Create Restaurant (ROLE_RESTAURANT_OWNER)**:
   * Log in as a user with `ROLE_RESTAURANT_OWNER` to get their JWT.
   * Send a `POST` request to `/api/restaurants` with restaurant data and the owner's JWT.
   * Verify the restaurant is created and associated with the owner.
* **Update Restaurant (ROLE_RESTAURANT_OWNER - Ownership Check)**:
   * Attempt to update a restaurant that belongs to a different owner. The request should be denied due to resource ownership checks.

### 3. Order Placement & Management

* **Place Order (ROLE_CUSTOMER)**:
   * Log in as a `ROLE_CUSTOMER` to get their JWT.
   * Send a `POST` request to `/api/orders` with order details (e.g., restaurant ID, menu items).
   * Verify the order is created with `PENDING` status.
* **View Own Orders (ROLE_CUSTOMER)**:
   * As the placing customer, send a `GET` request to `/api/orders/{orderId}` or `/api/orders`.
   * Verify only your orders are visible.
* **View Restaurant Orders (ROLE_RESTAURANT_OWNER)**:
   * Log in as a `ROLE_RESTAURANT_OWNER` and send a `GET` request to `/api/orders`.
   * Verify only orders for their specific restaurant are visible.

### 4. Event-Driven Notifications

* **Simulate Notification**:
   * Place an order as a `ROLE_CUSTOMER` (as above).
   * Check the console logs of the `notification-service` and `restaurant-service`. You should see logs indicating that an `OrderPlacedEvent` was received and processed (e.g., "Simulating email/push notification for order...", "Starting preparation for order...").

### 5. Resilience (Circuit Breaker)

* **Test Fallback**:
   1.  Ensure `discovery-server`, `config-server`, `api-gateway`, `auth-service`, `order-service`, `notification-service` are running.
   2.  **Stop `restaurant-service`**.
   3.  As a `ROLE_CUSTOMER`, attempt to place an order via the `api-gateway` (which will route to `order-service`).
   4.  The `order-service` will try to communicate with the `restaurant-service` (which is down), triggering its circuit breaker and fallback. You should receive a graceful fallback response instead of an error.
   5.  Check `order-service` logs for fallback messages.
   6.  Access `http://localhost:<order-service-port>/actuator/circuitbreakerevents` to see the circuit breaker status (e.g., `OPEN`).

## 🔑 JWT Testing with Postman

1.  **Obtain JWT**:
   * Make a `POST` request to `http://localhost:<api-gateway-port>/auth/login`.
   * In the `Body` tab, select `raw` and `JSON`, then provide:
       ```json
       {
           "email": "your_email@example.com",
           "password": "your_password"
       }
       ```
   * Copy the `token` value from the JSON response.

2.  **Set Postman Environment Variable**:
   * In your Postman environment, create a new variable named `JWT_TOKEN`.
   * Paste the copied JWT as its value.

3.  **Use JWT in Requests**:
   * For any subsequent requests to secured endpoints (e.g., `/api/restaurants`, `/api/orders`), go to the `Authorization` tab.
   * Select `Type: Bearer Token`.
   * In the `Token` field, enter `{{JWT_TOKEN}}`. Postman will automatically substitute this with your stored JWT.

## ▶️ Service Startup Order

It is crucial to start the microservices in the correct order to ensure proper service discovery and configuration loading:

1.  **`discovery-server`** (Eureka)
2.  **`config-server`**
3.  **Message Broker** (Kafka or RabbitMQ - ensure this is running before services that depend on it)
4.  **`auth-service`**
5.  **`api-gateway`**
6.  **`restaurant-service`**
7.  **`order-service`**
8.  **`notification-service`**

## 🔗 Links to Swagger UIs

Once the services are running, you can access their respective Swagger UI documentation for interactive API testing (replace with actual ports if different):

* **Auth Service**: `http://localhost:8085/swagger-ui.html`
* **Restaurant Service**: `http://localhost:8082/swagger-ui.html`
* **Order Service**: `http://localhost:8086/swagger-ui.html`
* **Notification Service**: `http://localhost:8084/swagger-ui.html`

## 🗺️ Architecture Diagram

A detailed architecture diagram illustrating the ByteBites microservices system would depict:

* **Client**: Interacting with the API Gateway.
* **API Gateway**: The single entry point, responsible for routing and initial security.
* **Core Services**:
   * `discovery-server` (Eureka)
   * `config-server`
   * `auth-service` (handling login, registration, JWT issuance)
* **Business Services**: `restaurant-service`, `order-service`, `notification-service`.
* **Message Broker**: Kafka/RabbitMQ, showing `order-service` publishing `OrderPlacedEvent` and `notification-service`/`restaurant-service` consuming it.
* **Database**: Each service potentially interacting with its own database or a shared persistence layer.
* **JWT Flow**: Showing JWT being issued by `auth-service`, sent by client, validated by `api-gateway`, and claims forwarded to downstream services.
* **Resilience4j**: Indication of circuit breakers on inter-service calls.