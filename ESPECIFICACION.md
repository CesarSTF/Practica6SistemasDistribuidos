# Especificación — Parte 2: Microservicios Spring Boot + RabbitMQ

## Arquitectura

4 proyectos Spring Boot independientes, mismo JDK 21, mismo Maven.

```
┌─────────────────────────────────────────────────────┐
│                   RABBITMQ                          │
│  cola.gps.telemetria  cola.alertas.temperatura      │
│  cola.combustible.nivel   cola.notificaciones       │
└────────┬──────────────┬──────────────┬──────────────┘
         │              │              │
         ▼              ▼              ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────────┐
│ fleet-gps-service│ │fleet-alert-svc │ │fleet-notification-svc│
│ GPS consumer    │ │Temp+Fuel consume│ │Notif queue consumer │
│ Persiste en DB  │ │Reenvía a notif  │ │Log/Email/SMS        │
└─────────────────┘ └─────────────────┘ └─────────────────────┘
                                              │
                                              ▼
                                    ┌─────────────────┐
                                    │   fleet-api      │
                                    │ REST /api/fleet/* │
                                    │ Consulta DB      │
                                    └─────────────────┘
```

## Proyecto 1: fleet-gps-service

**Package**: `com.fleet.gps`

**pom.xml**: parent spring-boot-starter-parent 4.0.6, Java 21. 
Dependencias: spring-boot-starter-amqp, spring-boot-starter-data-jpa, spring-boot-starter-web, h2 (runtime), spring-boot-starter-test (test).

**Archivos**:

- `FleetGpsApplication.java` — main class
- `config/RabbitMQConfig.java` — cola `cola.gps.telemetria` durable, exchange `exchange.fleet` direct, binding con routing key `gps.routing`
- `model/GpsMessage.java` — POJO: vehicleId, timestamp, lat, lng, speed
- `consumer/GpsConsumer.java` — `@RabbitListener(queues = "cola.gps.telemetria")`, log + persiste
- `service/GpsService.java` — procesa y persiste mensaje

**application.yml**:
```yaml
spring:
  rabbitmq: { host: localhost, port: 5672, username: guest, password: guest }
  datasource: { url: jdbc:h2:mem:fleetgps }
  jpa: { hibernate: { ddl-auto: update }, show-sql: true }
server: { port: 8081 }
```

---

## Proyecto 2: fleet-alert-service

**Package**: `com.fleet.alert`

**pom.xml**: mismo parent y Java. Mismas dependencias.

**Archivos**:

- `FleetAlertApplication.java` — main class
- `config/RabbitMQConfig.java` — colas `cola.alertas.temperatura` y `cola.combustible.nivel` (durables), exchange `exchange.fleet`, bindings con `temp.alert` y `fuel.routing`
- `consumer/TemperatureAlertConsumer.java` — `@RabbitListener(queues = "cola.alertas.temperatura")`, log warning, reenvía a `cola.notificaciones`
- `consumer/FuelAlertConsumer.java` — `@RabbitListener(queues = "cola.combustible.nivel")`, log warning, reenvía a `cola.notificaciones`

**application.yml**:
```yaml
spring:
  rabbitmq: { host: localhost, port: 5672, username: guest, password: guest }
  datasource: { url: jdbc:h2:mem:fleetalert }
  jpa: { hibernate: { ddl-auto: update } }
server: { port: 8082 }
```

---

## Proyecto 3: fleet-notification-service

**Package**: `com.fleet.notification`

**pom.xml**: mismo parent y Java. spring-boot-starter-amqp, spring-boot-starter-web, spring-boot-starter-test.

**Archivos**:

- `FleetNotificationApplication.java` — main class
- `config/RabbitMQConfig.java` — cola `cola.notificaciones` durable
- `consumer/NotificationConsumer.java` — `@RabbitListener(queues = "cola.notificaciones")`, log info con alerta recibida

**application.yml**:
```yaml
spring:
  rabbitmq: { host: localhost, port: 5672, username: guest, password: guest }
server: { port: 8083 }
```

---

## Proyecto 4: fleet-api

**Package**: `com.fleet.api`

**pom.xml**: mismo parent y Java. spring-boot-starter-web, spring-boot-starter-data-jpa, h2 (runtime), spring-boot-starter-test (test).

**Archivos**:

- `FleetApiApplication.java` — main class
- `model/FleetStatus.java` — POJO totalVehicles, activeAlerts, timestamp
- `controller/FleetController.java` — `@RestController @RequestMapping("/api/fleet")`
  - `GET /status` → `{totalVehicles:3, activeAlerts:2, timestamp:now}`
  - `GET /vehicle/{id}/telemetria` → `["Datos de telemetria para {id}"]`

**application.yml**:
```yaml
spring:
  datasource: { url: jdbc:h2:mem:fleetapi }
  jpa: { hibernate: { ddl-auto: update } }
server: { port: 8080 }
```

---

## Estructura final del workspace

```
Practica6SistemasDistribuidos/
├── mqtt/                          ← (ya existe, Parte 1)
├── fleet-gps-service/
│   ├── pom.xml
│   └── src/main/java/com/fleet/gps/
├── fleet-alert-service/
│   ├── pom.xml
│   └── src/main/java/com/fleet/alert/
├── fleet-notification-service/
│   ├── pom.xml
│   └── src/main/java/com/fleet/notification/
├── fleet-api/
│   ├── pom.xml
│   └── src/main/java/com/fleet/api/
├── docker-compose.yml
└── README.md
```

## RabbitMQ

Las colas y exchanges deben existir antes de arrancar los microservicios. El bridge Python (`mqtt_rabbitmq_bridge.py`) ya las declara al iniciar. Si se quiere independencia, cada microservicio declara sus colas en su propio `RabbitMQConfig.java`.
