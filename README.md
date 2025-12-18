# HiveMind - Security System Backend

Automated security system backend capable of monitoring and protecting the ecosystem.

## 📋 Overview

HiveMind combines real-time monitoring, AI-based anomaly detection, and automated responses.

**Architecture**:
```
Devices → DataStream (Kafka/Flink) → Backend (Spring Boot) → Database (Cassandra/PostgreSQL)
```

---

## 🚀 How to Run

### Prerequisites
1.  **Java 17+** installed.
2.  **Docker Desktop** installed and **running**.
3.  **Maven** installed (optional, can use `./mvnw`).

### Step 1: Start Infrastructure (Database & Messaging)
Navigate to the backend directory and start the services using Docker Compose:

```bash
docker-compose up -d
```
*This starts Cassandra (DB) and Kafka (Messaging).*

### Step 2: Run the Backend
Once the Docker services are running (wait a minute for them to initialize), start the application:

```bash
./mvnw spring-boot:run
```
*Or if you have Maven installed globally:*
```bash
mvn spring-boot:run
```

---

## 🛠️ Components
- **Backend**: Spring Boot
- **Database**: Cassandra (Port 9042)
- **Messaging**: Kafka (Port 9092)

## 📡 API Endpoints
- `GET /api/health`: Check system status.
- `GET /api/users`: List users.
- `POST /api/events`: Submit security event.

---
## 👥 Team
- **Frontend/DevOps**: Ahmed Rayen Thabet
- **Data Stream**: Adem Ben Romdhane
- **Security**: Malek Boujazza
- **AI**: Eya Skhiri
- **Backend**: Jasser Lefi
