@echo off
echo Starting Spring Boot NATS Demo...
docker-compose up -d
echo Waiting for services to be healthy...
timeout /t 5 /nobreak
docker-compose ps
echo Services started!
echo Gateway: http://localhost:8082
echo User Service: http://localhost:8081
echo Shipping Service: http://localhost:8084
echo NATS Monitoring: http://localhost:8222
echo REDIS cache: http://localhost:6379
echo MongoDB: http://localhost:27017/shippingdb
echo H2 Console: http://localhost:8081/h2-console
echo FrontShip: http://localhost:5173/
