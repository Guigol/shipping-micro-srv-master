# Complete tests guide - Spring Boot + NATS Project + User Service

## Table of contents
1. [Validation Checklist before Tests](#1-validation-checklist-before-tests)
2. [Maven Compilation Tests](#2-maven-compilation-tests)
3. [Tests with Docker (recommanded)](#3-tests-with-docker-recommanded)
4. [Login-Logout](#4-loginlogout)
5. [User Service CRUD Tests](#5-user-service-crud-tests)
6. [H2 Console & Database](#6-h2-console--database)
7. [Logs in real time](#7-Logs-in-real-time)
8. [Cleaning](#8-Cleaning)
9. [Additional Resources](#9--additional-resources)

---

## 1. Validation Checklist before Tests

### ✅ System prerequisites

- [ ] **Java 17**
  ```bash
  java -version
  ```
  Expected result : `java version "17.x.x"` or over

- [ ] **Maven 3.9+**
  ```bash
  mvn -version
  ```
  Expected result : `Apache Maven 3.9.x` or over

- [ ] **Docker is installed and running**
  ```bash
  docker --version
  ```
  Expected result : `Docker version 20.x.x` or over

- [ ] **Docker Compose**
  ```bash
  docker-compose --version
  ```
  Expected result : `docker-compose version 1.29.x` or `Docker Compose version v2.x.x`

---

- [ ] **Connected Ports**
  
  - **4222** : NATS Server
  - **8082** : Gateway Service
  - **8081** : User Service
  - **8222** : NATS Monitoring
  - **8084** : Shipping Service
  - **6379** : Redis
  - **5173** : Front End Shipping
  - **27017** : MongoDB Shipments
  
  ```bash
  # Windows
  netstat -ano | findstr "4222 8082 8081 8222 8084 5173"
  
  # Linux/Mac
  netstat -tuln | grep -E "4222|8082|8081|8222|8084|5173"
  lsof -i :4222
  lsof -i :8082
  lsof -i :8084
  lsof -i :8081
  lsof -i :8222
  lsof -i :5173
  ```
 

## 2. Maven Compilation Tests

### 2.1 Compilation tests

From basic-java-main folder, run :

```bash
mvn clean compile
```

**Example of Expected Layout :**
```
The original artifact has been renamed to /app/shipping-micro-service/target/shipping-micro-service-1.0.0.jar.original
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for Spring Boot NATS Demo 1.0.0:
[INFO]
[INFO] Spring Boot NATS Demo .............................. SUCCESS [  0.647 s]
[INFO] shipping-micro-service ............................. SUCCESS [ 31.258 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  36.212 s
[INFO] Finished at: 2025-12-29T02:39:20Z
[INFO] ------------------------------------------------------------------------
#36 DONE 39.9s
```

### 2.2 Package Test  

If the compilation succeeds, run the command below :

```bash
mvn clean package -DskipTests
```

**Check :**
- JAR files in :
  - `gateway/target/gateway-1.0.0.jar`
  - `user-service/target/user-service-1.0.0.jar`
  - `shipping-micro-service/target/hipping-micro-service-1.0.0.jar`
- Final Message: `BUILD SUCCESS`

### 2.3 Unit Test Execution

```bash
mvn clean test
```

**Note :** If no tests are implemented, Maven will simply indicate that there are no tests to run.

---

## 3. Tests with Docker (recommanded)

Docker allows you to test the application in an isolated and reproducible environment.

### 3.1 Build and Start

#### Windows

```bash
# Build Docker's images
build.bat

# Start services
start.bat
```

#### Linux/Mac

```bash
# Run scripts once (first time only)
chmod +x *.sh

# Build Docker's images
./build.sh

# Start services
./start.sh
```

**Expected result :**

1. **Build** :
   - Maven Compilation for each service
   - Docker's images creation: 
     - `shipping-micro-srv-master-gateway`
     - `mongo:7`
     - `shipping-micro-srv-master-user-service`
     - `shipping-micro-srv-master-shipping-service`
     - `redis:7`
     - `nats:2.10-alpine`
     - `shipping-micro-srv-master-front`
   - It can last for 2-5 minutes 
   

2. **Start** :
   - Start 7 containers : `nats`, `gateway`, `user-service`, `shipping-service`...
   - Services Initialization (10-30 seconds)
   - NATS service connection
   - Redis and MongoDb connection


### 3.2 Services state and containers

```bash
docker-compose ps
```

**Expected Result :**
```
NAME                     IMAGE                                        COMMAND                  SERVICE            CREATED          STATUS                             PORTS
FrontEnd_Shipping        shipping-micro-srv-master-front              "/docker-entrypoint.…"   front              33 seconds ago   Up 5 seconds                       0.0.0.0:5173->80/tcp, [::]:5173->80/tcp
gateway                  shipping-micro-srv-master-gateway            "sh -c 'java $JAVA_O…"   gateway            33 seconds ago   Up 5 seconds (health: starting)    0.0.0.0:8082->8082/tcp, [::]:8082->8082/tcp
mongo-shipping           mongo:7                                      "docker-entrypoint.s…"   mongo-shipping     34 seconds ago   Up 32 seconds (healthy)            0.0.0.0:27017->27017/tcp, [::]:27017->27017/tcp
nats-server              nats:2.10-alpine                             "docker-entrypoint.s…"   nats               34 seconds ago   Up 32 seconds (healthy)            0.0.0.0:4222->4222/tcp, [::]:4222->4222/tcp, 0.0.0.0:8222->8222/tcp, [::]:8222->8222/tcp
redis                    redis:7                                      "docker-entrypoint.s…"   redis              34 seconds ago   Up 32 seconds (healthy)            0.0.0.0:6379->6379/tcp, [::]:6379->6379/tcp
shipping-micro-service   shipping-micro-srv-master-shipping-service   "sh -c 'java $JAVA_O…"   shipping-service   34 seconds ago   Up 21 seconds (health: starting)   0.0.0.0:8084->8084/tcp, [::]:8084->8084/tcp
user-service             shipping-micro-srv-master-user-service       "sh -c 'java $JAVA_O…"   user-service       34 seconds ago   Up 21 seconds (healthy)            0.0.0.0:8081->8081/tcp, [::]:8081->8081/tcp
```

All the services must be `Up`. 


**Important Messages to look for :**

**Gateway :**
```
Started GatewayApplication in X.XXX seconds
Connected to NATS server at nats://nats:4222
Listening on subjects: user.*
```

**User Service :**
```
Started UserServiceApplication in X.XXX seconds
Connected to NATS server at nats://nats:4222
HikariPool-1 - Start completed
```

**NATS :**
```
Server is ready
Listening for client connections on 0.0.0.0:4222
```

**Shipping service :**

```
Bootstrapping Spring Data Reactive MongoDB repositories in DEFAULT mode.
Bootstrapping Spring Data Redis repositories in DEFAULT mode.
Shipping-service NATS URL = nats://localhost:4222
NATS connection established successfully
```

#### 3.2.3 Health Checks

```bash
# Gateway (Curl)
curl http://localhost:8082/actuator/health

# User Service
curl http://localhost:8081/actuator/health

# NATS
curl http://localhost:8222/healthz

# Shipping Service
curl http://localhost:8084/actuator/health
```
---
## 4. Login/Logout

Only Admin can CRUD the User database (SQL). Below the credentials to **login** :

```bash
curl -X POST http://localhost:8082/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@ship.com","password":"admin123"}'
```

```json
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
Logout :

```bash
curl -X POST http://localhost:8082/auth/logout \
 ```
**Response:**
`Logged out successfully`


## 5. User Service CRUD Tests

This section tests all the user's CRUD operation (Create, Read, Update, Delete) through the REST API Gateway.
A set of about 10 users loads automatically at the start of the application for both profiles (local and Docker).

This Dataloader is located in :

  `basic-java-main/user-service/src/main/java/com/example/userservice/config/DataLoader.java`

### 🧪 Test 1 : Create User

```bash
curl -X POST http://localhost:8082/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Dupont","password":"12345","email":"alice.dupont@ship.com","address":"12 rue du Néant - Le Plessis", "role":"USER"}'
```
**Response:**

- **Status Code :** `201 Created`
- **Response Body :**
  ```json
  {
    "userId": 9,
    "id": null,
    "name": "Alice Dupont",
    "password": null,
    "email": "alice.dupont@ship.com",
    "address": "12 rue du Néant - Le Plessis",
    "role": "USER",
    "createdAt": "2025-12-30T02:32:08.413228",
    "updatedAt": null,
    "blank": false
  }
  ```

**📝 Note :**
- To manage Shipments and Users, the generated Id becomes userId through the Nats Gateway service.
---

### 🧪 Test 2 : Get User by Id

```bash
curl http://localhost:8082/api/users/3
```

**Response :**

- **Status Code :** `200 OK`
- **Response Body :**
  ```json
  {
    "userId": 3,
    "id": null,
    "name": "Alex Térieur",
    "password": null,
    "email": "alex.terieur@ship.com",
    "address": " 90 rue de Lyon, Lyon",
    "role": "USER",
    "createdAt": "2025-12-30T02:33:18.0240335",
    "updatedAt": null,
    "blank": false
  }
  ```

---

### 🧪 Test 3 : All the Users to list

```bash
curl http://localhost:8082/api/users
```

**Response :**

- **Status Code :** `200 OK`
- **Response Body :**
  ```json
  [
    {
        "userId": 1,
        "id": null,
        "name": "Martha Desjambes",
        "password": null,
        "email": "martha.desjambes@ship.com",
        "address": "123 Rue de Paris, Paris",
        "role": "USER",
        "createdAt": "2025-12-30T02:33:35.9560933",
        "updatedAt": null,
        "blank": false
    },
    {
        "userId": 2,
        "id": null,
        "name": "Alain Térieur",
        "password": null,
        "email": "alain.terieur@ship.com",
        "address": "45 Avenue de Lyon, Lyon",
        "role": "USER",
        "createdAt": "2025-12-30T02:33:35.9566855",
        "updatedAt": null,
        "blank": false
    },
    {
        "userId": 3,
        "id": null,
        "name": "Alex Térieur",
        "password": null,
        "email": "alex.terieur@ship.com",
        "address": " 90 rue de Lyon, Lyon",
        "role": "USER",
        "createdAt": "2025-12-30T02:33:35.9566855",
        "updatedAt": null,
        "blank": false
    }
  ]
  ```

**📝 Note :**
- JSON Array returned


---

### 🧪 Test 4 : Update User

#### Linux/Mac

```bash
curl -X PUT http://localhost:8082/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Dupont","password":"12345","email":"alice.dupond@ship.com","address":"12 rue du Néant - Le Plessis", "role":"ADMIN"}'
```

**Response :**

- **Status Code :** `200 OK`
- **Response Body :**
  ```json
  {
   "id":1,
   "name": "Alice Dupond",
   "email": "alice.dupont@ship.com",
   "address": "12 rue du Néant - Le Plessis",
   "role": "ADMIN" 
  }
  ```


---

### 🧪 Test 5 : Asynchrone Notification

This test validates the async communication through NATS between the Gateway and the User Service.

#### Linux/Mac

```bash
curl -X POST http://localhost:8082/api/users/1/notify \
  -H "Content-Type: application/json" \
  -d '{"message":"Test de notification asynchrone"}'
```


**Response :**

- **Status Code :** `202 Accepted`
- **Response Body :**
  ```json
  {
    "status": "NOTIFICATION_SENT",
    "message": "Notification envoyée avec succès"
  }
  ```

**📝 Note :**
Check the User Service logs to check notification processed :

```bash
# Linux/Mac
docker-compose logs user-service | grep -i notification

# Windows
docker-compose logs user-service | findstr /i notification
```


---

### 🧪 Test 6 : Delete User by Id

```bash
curl -X DELETE http://localhost:8082/api/users/1
```

**Response :**

- **Status Code :** `204 No Content`
- **Response Body :** no content


---

### 🧪 Test 7 : Check the deletion

```bash
curl http://localhost:8082/api/users/1
```

**Response :**

- **Status Code :** `404 Not Found`
- **Response Body :**
  ```json
  {
    "timestamp": "2024-01-15T10:30:00.123",
    "status": 404,
    "error": "Not Found",
    "message": "User not found with ID: 1",
    "path": "/api/users/1"
  }
  ```

---
## 6. H2 Console & Database


The embedded H2 User Service database is accessible via a web interface

**URL :**
```
http://localhost:8081/h2-console
```

**Connection settings :**

- **JDBC URL :** `jdbc:h2:mem:userdb?createDatabaseIfNotExist=true`
- **Username :** `sa`
- **Password :** `sa`

**Useful SQL queries :**

```sql
-- List all the users
SELECT * FROM users;

-- Count the users
SELECT COUNT(*) FROM users;

-- Find a user by email
SELECT * FROM users WHERE email = 'alice.dupont@ship.com';

-- See structure table
SHOW COLUMNS FROM users;
```


---
## 7. Logs in real time

```bash
docker-compose logs -f
```

#### Each service

```bash
docker-compose logs -f gateway
docker-compose logs -f user-service
docker-compose logs -f nats
```

#### Filtering the logs

```bash
# Linux/Mac - Looking for errors
docker-compose logs | grep -i error

# Linux/Mac - Looking for NATS messages
docker-compose logs | grep -i nats

# Windows - Looking for errors
docker-compose logs | findstr /i error
```

---


## 8. Cleaning

### 8.1 Stop all the Services

```bash
# Linux/Mac
./stop.sh

# Windows
stop.bat

# Docker-compose
docker-compose down
```

---

### 8.2 Delete the volumes (Data)

To also delete persistent data :

```bash
docker-compose down -v
```

**Beware :** This command deletes all data stored in the database.

---

### 8.3 Complete Docker Cleaning


```bash
# Delete stopped containers
docker container prune -f

# Delete unused images
docker image prune -a -f

# Delete unused volumes
docker volume prune -f

# Complete cleaning (Remove everything)
docker system prune -a --volumes -f
```

---

### 8.4 Maven cleaning

To remove compiled artifacts :

```bash
mvn clean
```

This removes the `target/` folders from all modules.

---


## 9. 📚 Additional Resources

- **Spring Boot :** https://spring.io/projects/spring-boot
- **NATS :** https://docs.nats.io/
- **Docker Compose :** https://docs.docker.com/compose/
- **H2 Database :** https://www.h2database.com/

---

