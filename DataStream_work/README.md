# HiveMind DataStream - API Documentation

## 📋 Overview

Module de traitement de flux de données en temps réel pour le projet **Automated Security for the Ecosystem**.

**Technologies**: Apache Kafka, Apache Flink, Spring Boot, Docker

---

## 🔌 Pour Jasser (Backend Developer)

### 1. API REST pour recevoir les événements

**Endpoint**: `POST http://localhost:8080/api/events`

**Headers**: `Content-Type: application/json`

**Format JSON** (tous les champs sont requis):
```json
{
  "eventType": "LOGIN_FAILURE",
  "deviceId": "WS-001",
  "severity": "CRITICAL",
  "username": "alice",
  "authenticationStatus": "FAILURE",
  "deviceType": "WORKSTATION",
  "sourceIp": "192.168.1.100",
  "timestamp": "2025-12-04T10:00:00"
}
```

**Champs disponibles**:
- `eventType`: Type d'événement (ex: `LOGIN_FAILURE`, `DISK_FULL`, `SUSPICIOUS_ACTIVITY`)
- `deviceId`: Identifiant unique de l'appareil
- `severity`: `LOW`, `MEDIUM`, `HIGH`, ou `CRITICAL`
- `username`: Nom d'utilisateur associé à l'événement
- `authenticationStatus`: `SUCCESS`, `FAILURE`, ou `NONE`
- `deviceType`: `WORKSTATION`, `SERVER`, `IOT`, ou `NETWORK`
- `sourceIp`: Adresse IP source
- `timestamp`: ISO 8601 format

**Réponse**: `Event received and forwarded to device-events-{type}`

### 2. Health Check

**Endpoint**: `GET http://localhost:8080/api/health`

**Réponse**: `HiveMind DataStream API is running`

---

## 📊 Pour Malek (Sécurité/ELK)

### Topics Kafka disponibles

Pour consommer les événements:
- `device-events-workstation` - Événements des postes de travail
- `device-events-server` - Événements des serveurs
- `device-events-iot` - Événements IoT
- `device-events-network` - Événements réseau (routeurs, switches)

**Kafka Bootstrap Server**: `localhost:9094`

**Consumer Group suggéré**: `elk-consumer-group`

### Exemple de consommation (Logstash)

```conf
input {
  kafka {
    bootstrap_servers => "localhost:9094"
    topics => ["device-events-workstation", "device-events-server", "device-events-iot", "device-events-network"]
    group_id => "elk-consumer-group"
    codec => "json"
  }
}
```

---

## 🤖 Pour Eya (IA/Ollama)

### Option 1: Consommer depuis Kafka (Recommandé)

Tu peux lire les événements directement depuis les topics Kafka pour l'analyse IA.

### Option 2: Topic dédié pour les alertes

Je peux créer un topic spécial `high-severity-alerts` qui ne contiendra que les événements **HIGH** et **CRITICAL** pour optimiser ton traitement.

**Format des événements** (déjà en JSON, prêt pour Ollama):
```json
{
  "eventType": "SUSPICIOUS_ACTIVITY",
  "deviceId": "WS-001",
  "severity": "HIGH",
  "username": "alice",
  "authenticationStatus": "FAILURE"
}
```

---

## 🚀 Pour Rayen (DevOps/Frontend)

### Docker Compose

Tous les services sont conteneurisés:
- **Kafka**: Port 9094
- **Zookeeper**: Port 2181
- **Flink JobManager**: Port 8081 (Web UI)
- **Flink TaskManager**: Traitement interne
- **Spring Boot API**: Port 8080

### Commandes

```bash
# Démarrer tous les services
docker-compose up -d

# Arrêter
docker-compose down

# Voir les logs
docker logs -f kafka
docker logs -f datastream-work-taskmanager-1
```

### Pour le Dashboard React

Tu peux:
1. **Appeler l'API REST** pour soumettre des événements de test
2. **Consommer Kafka** via WebSocket pour afficher les alertes en temps réel
3. **Utiliser Flink Web UI** (http://localhost:8081) pour monitoring

---

## 📦 Données disponibles en sortie (pour tous)

Flink traite les événements et génère:
- **Alertes HIGH/CRITICAL** avec tous les détails (type, device, user, auth status)
- **Événements normaux** (LOW/MEDIUM) pour logging

### Prochaine étape

Je peux configurer Flink pour écrire dans:
- **Cassandra** (pour Jasser - stockage historique)
- **PostgreSQL** (pour Jasser - base relationnelle)
- **Topic Kafka dédié** (pour Eya - analyse IA)

---

## 🔧 Configuration requise

### Variables d'environnement

```bash
KAFKA_BOOTSTRAP_SERVERS=localhost:9094
```

### Ports utilisés

- `8080` - Spring Boot API
- `8081` - Flink Web UI
- `9094` - Kafka (externe)
- `2181` - Zookeeper

---

## 🚦 Quick Start

```bash
# 1. Cloner le repo
git clone https://github.com/iluvumua/HiveMind.git
cd HiveMind/DataStream-work

# 2. Démarrer Docker
docker-compose up -d

# 3. Créer les topics Kafka
for topic in device-events-workstation device-events-iot device-events-network device-events-server; do
  docker exec kafka kafka-topics --create --bootstrap-server kafka:29092 --topic $topic --partitions 1 --replication-factor 1 --if-not-exists
done

# 4. Build le projet
mvn clean package -DskipTests

# 5. Démarrer Spring Boot API
mvn spring-boot:run

# 6. Soumettre le job Flink (via Web UI http://localhost:8081)
# Uploader: target/flink-job.jar
# Entry Class: com.hivemind.datastream.DataStreamJob

# 7. Tester
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"eventType":"LOGIN_FAILURE","deviceId":"WS-001","severity":"CRITICAL","username":"alice","authenticationStatus":"FAILURE"}'
```

---

## 📝 TL;DR pour chaque membre

- **Jasser**: Utilise `POST http://localhost:8080/api/events` pour envoyer des événements
- **Malek**: Consomme les topics Kafka (`device-events-*`) pour ELK
- **Eya**: Lis depuis Kafka ou je crée un topic `alerts` pour toi
- **Rayen**: Tout est dockerisé, `docker-compose up -d` pour démarrer

---

## 👥 Équipe

**Ingénieur Data Stream**: Adem Ben Romdhane

**Projet**: Automated Security for the Ecosystem
