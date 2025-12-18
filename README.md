# HiveMind - Automated Security for the Ecosystem

> *"The caravan moves on, and the dogs bark"*

Système de sécurité automatisé et intelligent capable de surveiller et de protéger l'ensemble d'un écosystème réseau.

---

## 📋 Vue d'ensemble

HiveMind combine la surveillance en temps réel, la détection d'anomalies basée sur l'IA et des réponses automatisées pour garantir une sécurité continue de votre infrastructure.

**Couverture**: Ordinateurs, Serveurs, Routeurs, Commutateurs, Objets connectés (IoT)

---

## 🏗️ Architecture

```
Devices → DataStream (Kafka/Flink) → Backend (Spring Boot) → Database (Cassandra/PostgreSQL)
                ↓                              ↓
            ELK Stack                      AI (Ollama)
                ↓                              ↓
            Dashboard (React.js) ← WebSocket ← Alerts
```

---

## 🚀 Modules

### 📊 [DataStream](./DataStream-work) - Traitement en temps réel
**Responsable**: Adem Ben Romdhane

Collecte et traitement des événements en temps réel avec Apache Kafka et Apache Flink.

**API REST**: `POST http://localhost:8080/api/events`

**Topics Kafka**:
- `device-events-workstation`
- `device-events-server`
- `device-events-iot`
- `device-events-network`

[📖 Documentation complète](./DataStream-work/README.md)

---

### 🔐 Backend - Services & API
**Responsable**: Jasser Lefi

Services Spring Boot, API REST, intégration des bases de données et sécurité.

---

### 🔍 Security & ELK - Analyse des logs
**Responsable**: Malek Boujazza

Mise en place de la suite ELK, analyse des logs et détection des menaces.

#### 📊 ELK Stack - Rôle et Avantages

**ELK** (Elasticsearch, Logstash, Kibana) est le système central de collecte, traitement et visualisation des logs dans HiveMind.

##### 🎯 Rôle Principal

L'ELK Stack joue plusieurs rôles critiques dans l'écosystème HiveMind:

1. **Collecte Centralisée des Logs**: Agrège tous les logs provenant de différentes sources (IoT devices, services Spring Boot, alertes d'anomalies)
2. **Traitement et Enrichissement**: Transforme et normalise les données brutes en informations structurées
3. **Stockage Indexé**: Conserve les logs dans Elasticsearch pour des recherches rapides et efficaces
4. **Visualisation en Temps Réel**: Fournit des dashboards Kibana pour surveiller l'état du système

##### ✨ Avantages

- **🔍 Recherche Rapide**: Elasticsearch permet des recherches full-text sur des millions de logs en millisecondes
- **📈 Scalabilité**: Architecture distribuée capable de gérer des volumes massifs de données
- **⚡ Temps Réel**: Traitement et visualisation des logs en temps réel pour une réactivité maximale
- **🎨 Visualisation Puissante**: Kibana offre des dashboards interactifs et personnalisables
- **🔗 Intégration Kafka**: Consommation native des topics Kafka pour une intégration transparente
- **📊 Analyse Historique**: Stockage à long terme permettant l'analyse de tendances et la détection de patterns

##### 🔄 Processus de Traitement des Logs

```
IoT Devices (ESP32) → iot-device-service → Kafka (iot-logs topic)
                                              ↓
                                    anomaly-detection-service
                                              ↓
                                    Kafka (anomaly-alerts topic)
                                              ↓
                                         Logstash
                                              ↓
                                       Elasticsearch
                                              ↓
                                          Kibana
```

**Étapes détaillées**:

1. **Génération des Logs**: Les devices IoT (ESP32) envoient des logs via HTTP POST au `iot-device-service`
2. **Publication Kafka**: Le service publie les logs sur le topic `iot-logs`
3. **Détection d'Anomalies**: Le `anomaly-detection-service` consomme les logs, détecte les anomalies et publie des alertes sur `anomaly-alerts`
4. **Ingestion Logstash**: Logstash consomme le topic `anomaly-alerts` depuis Kafka
5. **Transformation**: Logstash applique des filtres (conversion timestamp, enrichissement)
6. **Indexation**: Les logs transformés sont indexés dans Elasticsearch avec un index quotidien (`anomaly-alerts-YYYY.MM.dd`)
7. **Visualisation**: Kibana interroge Elasticsearch pour afficher les dashboards en temps réel

##### 🔌 Intégration avec les Composants

**Avec Kafka**:
```yaml
# logstash.conf
input {
  kafka {
    bootstrap_servers => "kafka:29092"
    topics => ["anomaly-alerts"]
    group_id => "logstash-anomaly-group"
    codec => "json"
  }
}
```

**Avec Elasticsearch**:
```yaml
output {
  elasticsearch {
    hosts => ["http://elasticsearch:9200"]
    index => "anomaly-alerts-%{+YYYY.MM.dd}"
  }
}
```

**Avec les Services Spring Boot**:
- `iot-device-service` (port 8080): Génère et publie les logs IoT
- `anomaly-detection-service` (port 8081): Détecte les anomalies et génère des alertes

##### 📍 Endpoints et Ports

| Service | Port | URL | Description |
|---------|------|-----|-------------|
| Elasticsearch | 9200 | http://localhost:9200 | API REST et stockage |
| Kibana | 5601 | http://localhost:5601 | Interface de visualisation |
| Logstash | 5044, 9600 | - | Ingestion et monitoring |

##### 🚀 Configuration Docker

L'ELK Stack est déployé via Docker Compose avec les services suivants:

- **Elasticsearch**: Stockage distribué avec 256MB de heap
- **Kibana**: Interface web connectée à Elasticsearch
- **Logstash**: Pipeline de traitement connecté à Kafka et Elasticsearch

**Réseau**: Tous les services communiquent via le réseau `elk-network`

##### 📊 Exemple de Flux de Données

```json
// 1. Log IoT envoyé par ESP32
{
  "deviceId": "ESP32-001",
  "status": "ONLINE",
  "temperature": 85.5,
  "timestamp": "2025-12-18T19:46:40Z"
}

// 2. Alerte d'anomalie détectée
{
  "alertId": "uuid-123",
  "deviceId": "ESP32-001",
  "description": "Temperature exceeds threshold",
  "detectedValue": 85.5,
  "timestamp": 1734551200
}

// 3. Log indexé dans Elasticsearch
{
  "@timestamp": "2025-12-18T19:46:40.000Z",
  "alertId": "uuid-123",
  "deviceId": "ESP32-001",
  "description": "Temperature exceeds threshold",
  "detectedValue": 85.5
}
```

---

### 🤖 AI - Détection d'anomalies
**Responsable**: Eya Skhiri

Intégration d'Ollama pour l'analyse sémantique et la détection d'anomalies.

---

### 🎨 DevOps & Frontend
**Responsable**: Ahmed Rayen Thabet

Automatisation, déploiement, CI/CD et développement du tableau de bord React.

---

## 🛠️ Technologies

- **Data Streaming**: Apache Kafka, Apache Flink, MQTT
- **Backend**: Spring Boot, Spring Security
- **Databases**: Cassandra, PostgreSQL
- **Monitoring**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **AI**: Ollama
- **DevOps**: Docker, Kubernetes, Ansible
- **Frontend**: React.js

---

## 🚦 Quick Start

```bash
# 1. Cloner le projet
git clone https://github.com/iluvumua/HiveMind.git
cd HiveMind

# 2. Démarrer le module DataStream
cd DataStream-work
docker-compose up -d

# 3. Créer les topics Kafka
for topic in device-events-workstation device-events-iot device-events-network device-events-server; do
  docker exec kafka kafka-topics --create --bootstrap-server kafka:29092 --topic $topic --partitions 1 --replication-factor 1 --if-not-exists
done

# 4. Build et démarrer l'API
mvn clean package -DskipTests
mvn spring-boot:run

# 5. Tester l'API
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"eventType":"LOGIN_FAILURE","deviceId":"WS-001","severity":"CRITICAL","username":"alice","authenticationStatus":"FAILURE"}'
```

---

## 📡 API Endpoints

### DataStream API

**Base URL**: `http://localhost:8080`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/events` | Soumettre un événement de sécurité |
| GET | `/api/health` | Vérifier l'état de l'API |

**Exemple de payload**:
```json
{
  "eventType": "LOGIN_FAILURE",
  "deviceId": "WS-001",
  "severity": "CRITICAL",
  "username": "alice",
  "authenticationStatus": "FAILURE"
}
```

---

## 👥 Équipe

| Rôle | Nom |
|------|-----|
| DevOps & Frontend | Ahmed Rayen Thabet |
| Data Stream Engineer | Adem Ben Romdhane |
| Security Engineer | Malek Boujazza |
| AI Engineer | Eya Skhiri |
| Backend Developer | Jasser Lefi |

---

## 📚 Documentation

- [DataStream Module](./DataStream-work/README.md) - API Kafka/Flink
- [Backend API](#) - Services Spring Boot *(à venir)*
- [ELK Stack Configuration](#-elk-stack---rôle-et-avantages) - Monitoring, logs et visualisation
- [AI Integration](#) - Ollama setup *(à venir)*
- [Frontend Dashboard](#) - React.js *(à venir)*

---

## 📝 License

Projet académique - ENISO (École Nationale d'Ingénieurs de Sousse)

---

**Status**: 🟢 En développement actif
