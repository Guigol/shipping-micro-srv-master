# 🌐 Spring Boot + NATS - Jwt - MongoDb - Redis - ReactTypeScript  Project

# 🎯 Micro-Services Demo 

- #### User Service
- #### Gateway
- #### Shipping Service


![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=flat-square&logo=spring)
![NATS](https://img.shields.io/badge/NATS-2.10-blue?style=flat-square&logo=nats)
![Maven](https://img.shields.io/badge/Maven-3.9+-red?style=flat-square&logo=apache-maven)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=flat-square&logo=docker)


---

## 📋 Table of contents

- [🏗️ Architecture](#-Architecture)
- [🛠️Back end Technologies](#-Back-end-Technologies)
- [🛠️Front end Technologies](#-Front-end-Technologies)
- [🐳 Global Structure](#-Global-Structure)
- 📋 [Prerequisites](#-prerequisites)
- [⚡ Quick Start](#-Quick-Start)
- [🔍 Monitoring](#-Monitoring)
- [🧪 Usage, Users Stories](#-Usage-Users-Stories)
- [🔴 Login](#-Login)
- [🔐 JWT Token](#-JWT-Token)
- [🔄 Stop and Clean](#-Stop-and-Clean)
- [📝 Disclaimer](#-Disclaimer)

---

## 🏗️ Architecture

### 🌐 Microservices Architecture with Gateway Pattern
- Gateway REST API as unique entry point
- Isolated and independent Microservices features
- Inter-services communication via message broker


```
┌─────────────┐
│   Client    │
│   (HTTP)    │
└──────┬──────┘
       │
       │ REST API
       ▼
┌──────────────────────────────────────────┐
│             Gateway (Port 8082)          │
│  • Endpoints REST                        │
│  • HTTP queries validation               │
│  • NATS Communication (request-reply)    │
│  • Pointer exception and timeouts        │
└──────────────────┬───────────────────────┘
                   │
                   │ NATS Protocol
                   ▼
┌─────────────────────────────────────────────┐
│         NATS Server (Port 4222)             │
│  • Message broker                           │────────────────┐
│  • Request-Reply pattern                    │                │
│  • Publish-Subscribe pattern                │                │
│  • HTTP Monitoring (Port 8222)              │                │ NATS Protocol
└──────────────────┬──────────────────────────┘                │
                   │                                           │
                   │ NATS Protocol                             │
                   ▼                                           ▼
┌─────────────────────────────────────────────┐     ┌─────────────────────────────────────────────┐
│        User Service (Port 8081)             │     │       Shipping Service (Port 8084)          │
│  • NATS Listeners for CRUD                  │     │  • NATS Listeners for CRUD                  │         
│  • Features (UserService)                   │     │  • Features (ShippingService)               │
│  • JPA/H2 Persistance                       │     │  • MongoDb Persistance (Shipments & files)  │
│  • Notification                             │     │  • Redis cache                              │
└──────────────────┬──────────────────────────┘     └────────────────┬─────────────────┬──────────┘ 
                   │                                                 │                 │
                   │ JPA                                             │                 │
                   ▼                                                 ▼                 ▼  
              ┌─────────┐                                        ┌───────────┐      ┌─────────┐
              │ H2 DB   │                                        │ MongoDb   │      │ MongoDb │ 
              │ (Memory)│                                        │(Shipments)│      │ (Store) │
              └─────────┘                                        └────┬──────┘      └────┬────┘
                                                                      │                  │
                                                                      │     ┌─────────┐  │
                                                                      └──── │ Redis   │──┘      
                                                                            │ Cache   │        
                                                                            └─────────┘                 
                                  
          
```

---

## 🛠️ Back end Technologies

| Technology          | Version  | Usage                                    |
|---------------------|----------|------------------------------------------|
| **Java**            | 17       | Computing Language                       |
| **Spring Boot**     | 3.2.0    | Java Framework                           |
| **Spring Security** | 5.x    | Spring Security Java Framework           |
| **NATS**            | 2.10     | Broker Message                           |
| **NATS Spring**     | 0.5.6    | NATS/Spring Librairy                     |
| **Maven**           | 3.9+     | Build tools and dependancies  compilator |
| **H2 Database**     | Embedded | Memory Database                          |
| **Docker**          | Latest   | Containerization                         |
| **Docker Compose**  | 3.8      | Containerization administrator           |
| **Jackson**         | 2.15.3   | JSON Serialization/deserialization       |
| **Lombok**          | 1.18.30  | Boilerplate Constructor                  |
| **MongoDb**         | Latest   | NoSql Database                           |
| **Jwt**             | 0.11.5   | Authentication Security Token            |
| **Hibernate**       | 6.x      | ORM (via Spring Data JPA)                |
| **Redis**           | Latest   | Stored Cache                             |
---

## 🛠️ Front end Technologies

| Technology          | Version  | Usage                          |
|---------------------|----------|--------------------------------|
| **ReactTypeScript** | 18.3.1      | Computing Language Librairy    |
| **Typescript**      | 5.9.3    | Computing Language             |
| **Tailwindcss**     | 3.4.13     | Style                          |
| **vite**            | 7.2.4    | Bundle                         |
| **Formick**         | Latest   | Validation                     |
| **Axios**           | 1.13.2     | Fetch the data                 |
| **Docker**          | Latest   | Containerization               |
| **Docker Compose**  | 3.8      | Containerization administrator |

---

## 🏛️ Global Structure
```
📦shipping-micro-srv-master/
│
├─ basic-java-main
│   ├─ gateway/
│   │    ├─ Dockerfile
│   │    └─ src/main/resources/application-docker.yml
│   │
│   ├─ user-service/
│   │    ├─ Dockerfile
│   │    └─ src/main/resources/application-docker.yml
│   │
│   └─ shipping-micro-service/
│       ├─ Dockerfile
│       └─ src/main/resources/application-docker.yml
│
├─ FrontEnd_Shipping/
│   ├─ Dockerfile
│   └─ nginx.conf
│
└─ docker-compose.yml

```


---

## 📋 Prerequisites

- **JDK 17 or over** 
- **Maven 3.9 or over** 
- **Docker** 
- **Docker Compose** - (that comes with Docker Desktop)
- **Java IDE** - IntelliJ IDEA, Eclipse...
- **React IDE** - VsCode...

---

## ⚡ Quick Start

### Option 1 : With Docker (Best choice) 🐳

#### Linux / macOS

### Bash

```bash
 1. Clone the project from Git if necessary
# git clone <repository-url>

2. In the root directory (shipping-micro-srv-master), build and start all the services.
./build.sh
./start.sh

The front end Shipping Service can be reached at :
# http://localhost:5173
```

#### Windows

### Batch

```bash
1. Clone the project from Git if necessary
git clone https://github.com/Guigol/shipping-micro-srv-master.git

2. In the root directory (shipping-micro-srv-master), build and start all the services.
./build.bat
./start.bat

The Frontend Shipping Service can be reached at :
http://localhost:5173
```

### Option 2 : Run Locally (Development) 💻

```bash
1. Build Maven project
mvn clean package

2. Run NATS in terminal and keep it opened
docker run -p 4222:4222 -p 8222:8222 nats:2.10-alpine -js -m 8222

3. Run REDIS in another terminal and keep it opened
docker run --name redis -p 6379:6379 -d redis

4. Start User Service in another terminal
cd basic-java-main/user-service
mvn spring-boot:run

5. Start Gateway in another terminal
cd basic-java-main/gateway
mvn spring-boot:run

6. Start Shipping Service in onother terminal
cd basic-java-main/shipping-micro-service
mvn spring-boot:run

7. Start Frontend Shipping
Open FrontEnd_Shipping with vsCode
cd my-app
npm install
npm run dev
http://localhost:5173
```

---

## 🔍 Monitoring

| Service             | URL                                     | Description                |
|---------------------|-----------------------------------------|----------------------------|
| Gateway Health      | http://localhost:8082/actuator/health   | Gateway state              |
| User Service Health | http://localhost:8081/actuator/health   | User Service state         |
| NATS Monitoring     | http://localhost:8222                   | Monitoring NATS Interface  |
| NATS Connections    | http://localhost:8222/connz             | Active NATS Connection     |
| NATS Subscriptions  | http://localhost:8222/subsz             | Active NATS Subscriber     |
| H2 Console          | http://localhost:8081/h2-console        | H2 Database Console        |
| MongoDb/Shipments   | http://localhost:27017/shippingdb       | Shipments Database         |
| MongoDb/Store       | http://localhost:27017/shippingdb/store | Files Database             |
| Redis Cache         | http://localhost:6379                   | cache on Store & Shipments |

---
### H2 Console Settings


1. Clic on http://localhost:8081/h2-console
2. Connection settings :
   - **JDBC URL :** `jdbc:h2:mem:userdb?createDatabaseIfNotExist=true`
   - **User Name :** `sa`
   - **Password :** `sa`

**📝Note !** : H2 memory Database is meant only for tests, you can't use it in production. For this purpose (production), you must use a classic database as PostgreSql or Sql.
The H2 connection settings are provided for a quick start for tests with Docker, but you ought to change it for more security.

---

## 🧪 Usage, Users Stories

-  Both Admin and users can manage the parcels to ship:
   - They can create, update, track a shipment by TrackingNumber
   - Update the status of a shipment.
   - Upload / Download proofs of deposit, delivery... 
   - Only Admin can delete a shipment as the trash box appears when he's connected.
- The Admin can as well manage the users (full CRUD), a button [Gestion des Users] appears on the navbar when he's connected.
- Only the Admin sends Notification
- All users (USER and ADMIN) must log the application to use all functionalities.
- Proofs of Deposit and Delivery can be found in : `basic-java-main/shipping-micro-service/src/main/resources/proofs`
- When creating, updating or adding a shipment, the userId of whom is doing so is recorded in the shipment database.

**📝Note !** :
To be fully functional, the application must be connected to the other carriers API (DHL, COLISSIMO...)


---

## 🔑 Login
All users must connect by email and password. **12345** for users, **admin123** for Admin :
`POST http://localhost:8082/auth/login`

**User**
 ```json
  {
    "email": "jean.cive@ship.com",
    "password": "12345"
  }
  ```
Response
```json
{
  "role": "USER",
  "userId": 6,
  "email": "jean.cive@ship.com",
  "name": "Jean Cive"
}
```
**Admin**
```
  {
    "email": "admin@ship.com",
    "password": "admin123"
  }
```
**Response**
```json
{
  "name": "admin",
  "email": "admin@ship.com",
  "userId": 8,
  "role": "ADMIN"
}
```
---

## 🔐 JWT Token
```
AUTH_TOKEN=eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsInVzZ...; Path=/; Secure; HttpOnly; Expires=Thu, 12 Feb 2026 05:18:01 GMT;
```
**📝Note !** : To enforce security, the **Jwt** token is encapsulated within a cookie. Once authenticated, it goes smoothly from an http page to another.

- The **Jwt** lasts 15 minutes. ⏱️ After this time, you must reconnect to continue using the functionalities ⚠️.
- The session is stateless.
- To manage the cookie and avoid Cors issues, a **Docker profil** is set on the AuthController. 
- The authentication proceeds by a secret signingKey HS256 YOU MUST CHANGE located in :

   `basic-java-main/gateway/src/main/resources/application.yml`
  

---


## 🔄 Stop and Clean

### Stop the Services

```bash
# Linux/macOS
./stop.sh

# Windows
stop.bat

# Docker
docker-compose down
```

---

### Remove Containers and Volumes

```bash
# Stop and remove the Containers and Volumes
docker-compose down -v

# Remove images
docker-compose down -v --rmi all
```

---

### Complete Docker's Clean

> ⚠️ **Beware :** All Docker's containers, images and volumes will be deleted (not only this project)

```bash
# Stop all containers
docker stop $(docker ps -aq)

# Remove all containers
docker rm $(docker ps -aq)

# Remove all images
docker rmi $(docker images -q)

# Cleaning the system (recommended)
docker system prune -a --volumes
```

---

### Restart

```bash
# Stop and clean
docker-compose down -v

# Rebuild and restart
docker-compose up --build -d

# Check the state
docker-compose ps
```

---

## 📝 Disclaimer


```
Copyright (c) 2025

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
OTHER DEALINGS IN THE SOFTWARE.
```






