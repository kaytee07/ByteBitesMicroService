# 🚀 ByteBites: Secure Microservices Food Delivery Platform

## 📌 Overview
A cloud-native microservices architecture for ByteBites, built with **Spring Boot**, **Spring Cloud**, **JWT/OAuth2**, and **Kafka/RabbitMQ**. Implements RBAC, event-driven communication, and resilience patterns.

---

## 🏗️ **Architecture**
![Architecture Diagram](./docs/architecture.png) *(Replace with actual diagram path)*

### 🔗 **Core Services**
| Service                | Description                                  | Port  |
|------------------------|----------------------------------------------|-------|
| `discovery-server`     | Eureka Service Registry                     | 8761  |
| `config-server`        | Centralized Configuration (Git-backed)      | 8888  |
| `api-gateway`          | JWT Validation + Routing                    | 8080  |
| `auth-service`         | User Auth + JWT Issuance                    | 9001  |
| `restaurant-service`   | Restaurant/Menu Management                  | 9002  |
| `order-service`        | Order Processing                            | 9003  |
| `notification-service` | Handles Email/Push Notifications            | 9004  |

---

## 🔐 **Security Flow**
1. **Login**: `POST /auth/login` → Returns JWT
2. **Gateway**: Validates JWT → Adds `X-User-Id` and `X-Roles` headers
3. **RBAC**: Services enforce `@PreAuthorize("hasRole('ROLE_CUSTOMER')")`

### 👥 **Roles**
- `ROLE_CUSTOMER`: Place orders, view own history
- `ROLE_RESTAURANT_OWNER`: Manage restaurant/menu
- `ROLE_ADMIN`: Access `/admin/**` endpoints

---

## ⚙️ **Setup**
### Prerequisites
- Java 17+, Docker, Kafka, Git

### 🚀 **Run Services**
1. **Start Infrastructure**:
   ```bash
   docker-compose -f docker/kafka-zookeeper.yml up -d  # Or RabbitMQ

### 🚀 **Lunch Services In Order*

discovery-server → config-server → auth-service → api-gateway → other-services

## 🛠️ Testing

🔑 JWT Login

```bash
curl -X POST http://localhost:9001/auth/login \
-H "Content-Type: application/json" \
-d '{"username":"taylor", "password":"password"}' 



