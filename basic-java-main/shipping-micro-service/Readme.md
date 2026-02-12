# 🌐 Shipping-Micro-Service
## MongoDb, Redis, Test Junit, CRUD

---

## 📋 Table of contents

- [📡Detailed Structure](#-Detailed-Structure)
- [🛠️ Technologies](#-Technologies)
- [💡 Tests](#-Tests)
- [📚 Generated data example](#-Generated-data-example)
- [📖 Scope](#-Scope)
- [🐛 Diagram Flow](#-Diagram-Flow)
- [📄 ShipmentDataLoader](#-shipmentdataloader-)
- [🎯 Run shipping-micro-service](#-Run-shipping-micro-service)
- [💾 REDIS](#-REDIS)
- [📦 NATS Interaction](#-NATS-interaction) 
- [🌐 User Service Interaction](#-User-service-interaction)

---
## 📡 Detailed Structure

```
spring-boot-nats-demo/
├── gateway/
│   └── ...
├── user-service/
│   └── ...
│   └── ...
│   
└── shipping-micro-service/
│   ├── pom.xml
│   ├── Dockerfile
│   ├── HELP.md
│   ├── Readme.md
│   └── src/main/java/com/example/shippingservice/
│       ├── ShippingServiceApplication.java
│       │
│       ├── config/
│       │   ├── NatsConfig.java
│       │   ├── MongoConfig.java│   
│       │   ├── RedisConfig.java
│       │   └── ShipmentDataLoader.java
│       │
│       ├── dtos/
│       │   ├── ShipmentRequest.java
│       │   ├── ShipmentResponse.java
│       │   ├── TrackingResponse.java
│       │   ├── UserDto.java
│       │   ├── FileInfo.java
│       │   ├── UserDtoDeserializer.java
│       │   ├── ContactInfo.java
│       │   ├── AddTrackingStusRequest.java
│       │   └── ProofUploadRequest.java
│       │
│       │
│       ├── entities/
│       │   └── Shipment.java
│       │   └── StoredFile.java
│       │
│       ├── repositories/
│       │   └── ShipmentRepository.java
│       │   └── StoredFileRepository.java
│       │   
│       ├── services/
│       │   ├── ShipmentService.java
│       │   ├── TrackingService.java
│       │   └── FileStorageService.java
│       │
│       ├── listener/
│       │   ├── NatsErrorResponseFactory.java
│       │   └── ShippingNatsListener.java
│       │
│       ├── mappers/
│       │   └── ShipmentMapper.java
│       │
│       └── exception/
│           ├── ErrorDescriptor.java
│           ├── ErrorMessages.java
│           ├── ExceptionMapper.java
│           ├── GlobalExceptionHandler.java
│           ├── InvalidShipmentException.java
│           └── ShipmentNotFoundException.java
│
│
└────────────────────

```
---

## 🛠️ Technologies

```
[ Shipping Service ]
       │
       ├── NATS    → Gateway Entry point
       ├── MongoDB → Persistent data (shipments)
       ├── MongoDB → Files (labels, proofs...)
       ├── Redis   → Cache (quick access tracking states)
       └── Junit   → Tests

```

| Type        | Technology          | Usage                                                                          | Data Examples                                                 |
|-------------|---------------------|--------------------------------------------------------------------------------|---------------------------------------------------------------|
| **MongoDB** | Principal Store Data | Store the **shipments** and their steps history                                | `sender`, `receiver`, `carrier`, `status_history`, `metadata` |
| **Redis**   | Real Time Cache     | Speed up retrieving the **last status** (tracking) to handle upcoming delivery | `tracking:XYZ → {status, eta}`                                |
| **MongoDB** | Files storage       | Keep **documents and proofs** linked to the shipments                          | PDF labels, photos, signatures, receits                       |
| **NATS**    | Unique Entry Point  | All shipments queries are validated by Nats Entry Point                        | All queries                                                   |
| **Junit**   | Test Implementation | Redis, FileStorage, ShippingServiceApplication              | Validation tests                                              |

---

## 💡 Tests
3 end-to-end JUnit tests are available in :


`basic-java-main/shipping-micro-service/src/test/java/com/example/shippingService`

- FileStorageServiceTest.java
- RedisMongoIntegrationTest.java
- ShippingMicroServiceApplicationTests.java

Those tests must be run locally (no Docker) one by one  after Nats and Redis are listening on their port.
- `docker run -p 4222:4222 -p 8222:8222 nats:2.10-alpine -js `
- `docker run --name redis -p 6379:6379 -d redis`

---

## 📚 Generated data example

 ```json

{
  "_id": "SHIP-2025-0001",
  "sender": {
    "id": "USER-42",
    "name": "Alice Dupond",
    "address": "10 rue des Lilas, Lyon"
  },
  "receiver": {
    "id": "USER-77",
    "name": "Bob Dylan",
    "address": "5 avenue du Port, Marseille"
  },
  "carrier": "La Poste",
  "tracking_number": "LP123456789FR",
  "current_status": "IN_TRANSIT",
  "status_history": [
    {"status": "CREATED", "changed_at": "2025-11-03T08:00:00Z"},
    {"status": "SHIPPED", "changed_at": "2025-11-03T10:00:00Z"}
  ],
  "metadata": {
    "weight_kg": 2.3,
    "dimensions_cm": [30, 20, 15],
    "service": "Colissimo"
  },
  "files": {
    "label_url": "https://store.com/labels/SHIP-2025-0001.pdf",
    "proof_url": "https://store.com/proofs/SHIP-2025-0001.jpg"
  },
  "created_at": "2025-11-03T07:55:00Z",
  "updated_at": "2025-11-03T10:00:00Z"
}
```
---
## 📖 Scope
| Action              | Description                                            |
|---------------------|--------------------------------------------------------|
| **Create shipment** | Create shipment with sender, receiver, weight, carrier |
| **Generate docs**   | Retrieve or generate files via API Shipping            |
| **Tracking update** | Webhooks from carrier or polling                       |
| **Stored Files**    | Labels, proofs, invoices... (base64)                   |
| **Cache**           | Last status quick access, position...                  |

---

## 🐛 Diagram Flow
```
[ Client / Gateway ]
         ↓
[ Shipping API ]
         ↓
┌──────────────────────────┐
│ ShipmentManager          │ → MongoDB
│ TrackingUpdater          │ → Redis
│ FileManager              │ → MongoDB/Redis
└──────────────────────────┘
```
```
         ┌───────────────────┐
         │   Gateway API     │
         │  (Spring Boot)    │
         └────────┬──────────┘
                  │ HTTP GET /api/shipping/tracking/{TrackingNumberid}
                  ▼
         ┌───────────────────┐
         │ShippingController │
         │  (gateway → NATS) │
         └────────┬──────────┘
                  │ Publish NATS subject  "shipping.tracking.get"
                  ▼
         ┌──────────────────────────┐
         │     ShippingNatsListener │
         │     (receives NATS msg)  │
         └────────┬─────────────────┘
                  │ call
                  ▼
         ┌───────────────────┐
         │  TrackingService  │
         │ - getTracking(id) │
         │ - history/status  │
         └────────┬──────────┘
                  │ read/write
                  ▼
         ┌───────────────────┐
         │ShipmentRepository │
         │      (MongoDB)    │
         └───────────────────┘

```
---

## 📄 ShipmentDataLoader 

### (Docker's profile only)

A set of sample shipment's parcels loads automatically at the start of the application with Docker. If you wish to use it locally, you may set a local profile in the application.yml.

**"ShipmentDataLoader.java"** is located in :

`basic-java-main/shipping-micro-service/src/main/java/com/example/shippingService/config/ShipmentDataLoader.java`

---


## 🎯 Run shipping-micro-service

Once NATS service is up :
- Run Redis (see below)
- `cd basic-java-main/shipping-micro-service`
- `mvn spring-boot:run`
---

## 💾 REDIS
For this project, a **RedisConfig** is located at :


`basic-java-main/shipping-micro-service/src/main/java/com/example/shippingService/config/RedisConfig.java`

### Run locally
`docker run --name redis -p 6379:6379 -d redis`

### Test
`docker exec -it redis redis-cli ping`

### Keys
`docker exec -it redis redis-cli keys '*'`
#### Response :
```
1) "storedFiles::SHIP-122859DA-8ECA-4:deliveryProof"
2) "storedFiles::SHIP-B4A525BB-C105-4:depositProof"
```

### Docker Image :
`docker run -d --name redis-local -p 6379:6379 redis:alpine`

### Connecting to Mongo stored_files with Docker
`docker exec -it mongo-shipping mongosh -u root -p secret --authenticationDatabase admin`

`use shippingdb`

`db.stored_files.find().pretty()
`
---
## 📦 NATS Interaction

### Run locally
`docker run -p 4222:4222 -p 8222:8222 nats:2.10-alpine -js -m 8222`

As the only entry point, the Gateway broadcasts through Nats all the queries for the Shipping Service.
**Request-Reply** pattern is used for all CRUD operations where a response is expected.

```
Client → Gateway → [NATS: shipping.getAll] → Shipping Service → Database
Client ← Gateway ← [NATS: response]  ← Shipping Service ← Database
```
**Usage :**
- `shipping.create` - Create shipment
- `shipping.getAll` - All the shipment to list
- `shipping.getByShipmentId` - Retrieve shipment by Id
- `shipping.update` - Modify shipment
- `shipping.delete` - Delete shipment
- `shipping.tracking.get` - Get status from shipment
- `shipping.tracking.add` - Add a status to shipment


---
## 🌐 User Service Interaction

Shipping Service and User service are quite independent to each other. User service intervins only through gateway to bring back the userId from the user who's connected and record it in the shipment database if created or modified.

⚠️ Nevertheless, User Service must be up to validate user login from the "users" database. As axplained above, all the Shipping Service queries will pass through the Gateway.

---

