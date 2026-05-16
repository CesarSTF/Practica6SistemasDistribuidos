# Especificación: Parte 2 — Microservicios Spring Boot + RabbitMQ

## 1. Correcciones en `pom.xml`

| Actual | Correcto |
|--------|----------|
| `spring-boot-h2console` | **Eliminar** (no existe) |
| `spring-boot-starter-webmvc` | **`spring-boot-starter-web`** |
| `spring-boot-starter-webmvc-test` | **`spring-boot-starter-test`** |
| `spring-boot-starter-amqp-test` | **Eliminar** |
| `spring-boot-starter-data-jpa-test` | **Eliminar** |

Mantener Spring Boot 4.0.6 y Java 21.

---

## 2. Archivos a crear

### 2.1 `application.yml` en `src/main/resources/`
Reemplazar `application.properties`. Configurar:
- `spring.rabbitmq.host=localhost`, port=5672, user=guest, pass=guest
- `spring.h2.console.enabled=true`
- `spring.datasource.url=jdbc:h2:mem:fleetdb`
- `spring.jpa.hibernate.ddl-auto=update`, `show-sql=true`
- `server.port=8080`

### 2.2 `RabbitMQConfig.java` en `com.fleet.monitor.config`
- `@Configuration`
- Constantes: `GPS_QUEUE`="cola.gps.telemetria", `TEMP_ALERT_QUEUE`="cola.alertas.temperatura", `FUEL_QUEUE`="cola.combustible.nivel", `NOTIFICATION_QUEUE`="cola.notificaciones", `FLEET_EXCHANGE`="exchange.fleet"
- Beans: `DirectExchange fleetExchange()`, 4× `Queue`(durable=true), 3× `Binding`(gps→"gps.routing", temp→"temp.alert", fuel→"fuel.routing")

### 2.3 `GpsMessage.java` en `com.fleet.monitor.model`
POJO: `vehicleId`(String), `timestamp`(String), `lat`(double), `lng`(double), `speed`(double). Getters/setters.

### 2.4 `GpsConsumer.java` en `com.fleet.monitor.consumer`
- `@Component`, Logger
- `@RabbitListener(queues = "cola.gps.telemetria")` → log + parse + almacenar (try/catch)

### 2.5 `AlertConsumer.java` en `com.fleet.monitor.consumer`
- `@Component`, inyecta `RabbitTemplate` por constructor
- `@RabbitListener(queues = "cola.alertas.temperatura")` → log warn + reenvía a `cola.notificaciones`
- `@RabbitListener(queues = "cola.combustible.nivel")` → log warn + reenvía a `cola.notificaciones`

### 2.6 `FleetController.java` en `com.fleet.monitor.controller`
- `@RestController`, `@RequestMapping("/api/fleet")`
- `GET /status` → `{totalVehicles:3, activeAlerts:2, timestamp:Date}`
- `GET /vehicle/{id}/telemetria` → `["Datos de telemetria para {id}"]`

---

## 3. Estructura resultante

```
fleet-monitor/src/main/java/com/fleet/monitor/
├── config/RabbitMQConfig.java
├── model/GpsMessage.java
├── consumer/GpsConsumer.java
├── consumer/AlertConsumer.java
├── controller/FleetController.java
└── fleet_monitor/FleetMonitorApplication.java  (ya existe)

fleet-monitor/src/main/resources/
└── application.yml  (reemplaza application.properties)
```

## 4. Dependencias finales en `pom.xml`

Deben quedar solo: `spring-boot-starter-amqp`, `spring-boot-starter-data-jpa`, `spring-boot-starter-web`, `h2`(runtime), `spring-boot-starter-test`(test).
