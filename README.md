# Sistema de Monitoreo de Flota Logística - Arquitectura Distribuida

**Práctica 6 - Sistemas Distribuidos - Unidad 1**

![Status](https://img.shields.io/badge/Status-Complete-brightgreen)
![Architecture](https://img.shields.io/badge/Architecture-Event%20Driven-blue)
![License](https://img.shields.io/badge/License-MIT-green)

## 📋 Descripción General

Sistema completo de monitoreo en tiempo real de una flota de vehículos logísticos utilizando **arquitectura distribuida basada en eventos**. Integra IoT (MQTT), procesamiento de mensajes (RabbitMQ) y microservicios (Spring Boot) para crear un pipeline distribuido de telemetría GPS.

### Objetivos Alcanzados ✅

- ✅ Pipeline completo de datos: Sensores → MQTT → Bridge Python → RabbitMQ → Microservicios → API REST
- ✅ Monitoreo en tiempo real con actualizaciones cada 3 segundos
- ✅ Dashboard web interactivo con Bootstrap 5
- ✅ 3 servicios microservicios de Spring Boot en Docker
- ✅ Integración AMQP con RabbitMQ para mensajería distribuida
- ✅ Consumidor asincrónico de mensajes GPS
- ✅ API REST con 5 endpoints funcionales
- ✅ Almacenamiento en memoria thread-safe con ConcurrentHashMap

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                    CAPA IoT - SENSORES                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐   │
│  │ Sensor 1 │  │ Sensor 2 │  │ Sensor 3 │  │ Dashboard Web │  │
│  │  (GPS)   │  │  (GPS)   │  │  (GPS)   │  │  (HTTP/WS)   │  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────┘   │
│        │              │              │              ▲           │
│        └──────────────┼──────────────┘              │           │
│                       ▼ MQTT                        │           │
├─────────────────────────────────────────────────────┼───────────┤
│         BROKER MQTT - MOSQUITTO (Puerto 1883)      │           │
│  Topics:                                           │           │
│  • flota/VH-001/gps                               │           │
│  • flota/VH-002/gps                               │           │
│  • flota/VH-003/gps                               │           │
│                                                    │           │
│  ┌────────────────────────────────────────┐       │           │
│  │  Python Subscriber: Consume Topics     │       │           │
│  │  Storage: Base de datos SQLite         │       │           │
│  └────────────────────────────────────────┘       │           │
│        ▼ AMQP                                     │           │
├─────────────────────────────────────────────────────┼───────────┤
│    SERVICIO PUENTE - MQTT a RabbitMQ              │           │
│    (mqtt_rabbitmq_bridge.py)                      │           │
│    • Consume MQTT Topics                          │           │
│    • Transforma JSON                              │           │
│    • Publica en RabbitMQ Queues                   │           │
│        ▼ AMQP (Puerto 5672)                       │           │
├─────────────────────────────────────────────────────┼───────────┤
│    RABBITMQ MESSAGE BROKER (Puerto 5672/15672)    │           │
│    Exchange: exchange.fleet (DirectExchange)      │           │
│    Queue: cola.gps.telemetria (Routing: gps.*) ◄──┘           │
│    Queue: cola.alertas.temperatura                │           │
│    Queue: cola.combustible.nivel                  │           │
│    Queue: cola.notificaciones                     │           │
│        ▼ Spring AMQP Consumers                    │           │
├─────────────────────────────────────────────────────┼───────────┤
│           MICROSERVICIOS SPRING BOOT               │           │
│                                                    │           │
│  ┌────────────────────────────────────────────┐   │           │
│  │ FLEET-API (Puerto 8080) - PRINCIPAL        │   │           │
│  │ • GpsConsumer @RabbitListener               │   │           │
│  │ • FleetService: Almacenamiento en memoria  │   │           │
│  │ • FleetController: 5 Endpoints REST         │   │           │
│  │ • IndexController: Sirve Dashboard web      │   │           │
│  └────────────────────────────────────────────┘   │           │
│                                                    │           │
│  ┌────────────────────────────────────────────┐   │           │
│  │ FLEET-GPS-SERVICE (Puerto 8081)            │   │           │
│  │ • Procesa datos GPS                        │   │           │
│  │ • Calcula rutas óptimas                    │   │           │
│  └────────────────────────────────────────────┘   │           │
│                                                    │           │
│  ┌────────────────────────────────────────────┐   │           │
│  │ FLEET-ALERT-SERVICE (Puerto 8082)          │   │           │
│  │ • Evalúa alertas y umbrales                │   │           │
│  │ • Monitorea condiciones anormales          │   │           │
│  └────────────────────────────────────────────┘   │           │
│                                                    │           │
│  ┌────────────────────────────────────────────┐   │           │
│  │ FLEET-NOTIFICATION-SERVICE (Puerto 8083)   │   │           │
│  │ • Envía notificaciones al operador          │   │           │
│  │ • Integración email/SMS                     │   │           │
│  └────────────────────────────────────────────┘   │           │
└─────────────────────────────────────────────────────┼───────────┘
                                                     │ HTTP/REST
                                                     ▼
┌─────────────────────────────────────────────────────────────────┐
│           CLIENTE WEB - DASHBOARD (Bootstrap 5)                │
│  • Visualización en tiempo real                                │
│  • Grid responsive de vehículos                               │
│  • Indicadores de velocidad (color-coded)                    │
│  • Estadísticas agregadas                                    │
│  • Actualización automática cada 3 segundos                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Inicio Rápido

### Requisitos

- Docker 20.10+
- Docker Compose 2.0+
- Linux/macOS

### Ejecutar el Sistema Completo

```bash
# 1. Clonar/Navegar al directorio del proyecto
cd "/home/cesar/SEXTO CICLO/SISTEMAS DISTRIBUIDOS/UNIDAD1/Practica6SistemasDistribuidos"

# 2. Compilar e iniciar todos los contenedores
sudo docker compose build
sudo docker compose up -d

# 3. Verificar que todos los servicios estén arriba
sudo docker compose ps

# 4. Acceder al dashboard
# En navegador: http://localhost:8080
```

---

## 📡 Componentes Principales

### 1. **Sensores IoT (Simulados)**
- Archivo: `mqtt/Sensor_simulador.py`
- Simula 3 vehículos (VH-001, VH-002, VH-003)
- Publica GPS a MQTT cada 2 segundos
- Coordenadas: Quito, Ecuador con variación aleatoria

### 2. **Broker MQTT - Mosquitto**
- Puerto: 1883 (MQTT)
- Topics: `flota/{vehiculo_id}/gps`
- Almacenamiento en memoria de mensajes

### 3. **Servicio Puente Python**
- Archivo: `mqtt/mqtt_rabbitmq_bridge.py`
- Consume MQTT Topics
- Publica en RabbitMQ Exchange: `exchange.fleet`
- Routing key: `gps.routing` → `cola.gps.telemetria`

### 4. **RabbitMQ Message Broker**
- Puerto: 5672 (AMQP), 15672 (Management UI)
- Management: http://localhost:15672 (admin/admin123)
- Credenciales: admin/admin123
- Queue: `cola.gps.telemetria` (durable)
- Exchange: `exchange.fleet` (DirectExchange)

### 5. **Fleet-API (Microservicio Principal)**
- Puerto: 8080
- Framework: Spring Boot 3.4.4, Java 21
- Funciones:
  - GpsConsumer: Escucha RabbitMQ
  - FleetService: Almacenamiento en memoria (thread-safe)
  - FleetController: 5 endpoints REST
  - Dashboard web interactivo

### 6. **Dashboard Web**
- URL: http://localhost:8080
- Tecnología: HTML5, Bootstrap 5, Vanilla JavaScript
- Actualización: Cada 3 segundos vía Fetch API
- Mostrada: 3 tarjetas de vehículos con ubicación, velocidad y estado

---

## 📊 Flujo de Datos

```
1. SENSOR IoT → Publica MQTT (flota/VH-001/gps)
2. MOSQUITTO → Almacena y distribuye
3. PYTHON BRIDGE → Consume MQTT, Transforma, Publica en RabbitMQ
4. RABBITMQ → Enruta a cola.gps.telemetria
5. GPSCONSUMER → Deserializa JSON a GpsMessage
6. FLEETSERVICE → Actualiza ConcurrentHashMap
7. FLEETCONTROLLER → Consultas HTTP/REST
8. DASHBOARD → Fetch cada 3s, Renderiza tarjetas
```

---

## 📡 API REST - Endpoints

### Base URL
```
http://localhost:8080/api/fleet
```

### Endpoints Disponibles

#### 1. Health Check
```
GET /api/fleet/health
Response: {"status":"UP","service":"Fleet API"}
```

#### 2. Estado de la Flota
```
GET /api/fleet/status
Response: {
  "totalVehicles": 3,
  "activeAlerts": 0,
  "timestamp": "2026-05-17T01:33:06.018+00:00"
}
```

#### 3. Lista de Todos los Vehículos
```
GET /api/fleet/vehicles
Response: [
  {
    "vehicleId": "VH-001",
    "latitude": -2.168715,
    "longitude": -79.914435,
    "speed": 77.7,
    "lastUpdate": "2026-05-17T01:33:06"
  },
  ...
]
```

#### 4. Ubicación de Vehículo Individual
```
GET /api/fleet/vehicle/{id}
GET /api/fleet/vehicle/VH-001
Response: {"vehicleId":"VH-001",...} o 404
```

#### 5. Telemetría Formateada
```
GET /api/fleet/vehicle/{id}/telemetria
Response: ["Vehículo: VH-001 | Posición: (-2.168715, -79.914435) | Velocidad: 77.7 km/h ..."]
```

### Ejemplos con cURL

```bash
# Verificar salud
curl http://localhost:8080/api/fleet/health

# Ver estado de flota
curl http://localhost:8080/api/fleet/status | jq .

# Ver todos los vehículos
curl http://localhost:8080/api/fleet/vehicles | jq .

# Ver vehículo específico
curl http://localhost:8080/api/fleet/vehicle/VH-001 | jq .
```

---

## 🔧 Gestión con Docker Compose

```bash
# Compilar y lanzar
sudo docker compose build
sudo docker compose up -d

# Ver estado
sudo docker compose ps

# Ver logs
sudo docker compose logs -f fleet-api

# Logs de servicio específico
sudo docker compose logs -f fleet-api

# Reconstruir sin cache
sudo docker compose build --no-cache
sudo docker compose up -d

# Detener
sudo docker compose down
```

---

## 📝 Estructura del Código

### fleet-api/src/main/java/com/fleet/api/

```
config/
├── RabbitMQConfig.java          # Exchange, Queue, Binding
consumer/
├── GpsConsumer.java             # @RabbitListener
controller/
├── FleetController.java         # 5 endpoints REST
├── IndexController.java         # Ruta "/" → index.html
model/
├── GpsMessage.java              # DTO MQTT
├── VehicleLocation.java         # Ubicación actual
├── FleetStatus.java             # Estado agregado
service/
├── FleetService.java            # Lógica de negocio
```

### Clases Principales

#### **GpsConsumer.java**
```java
@Component
public class GpsConsumer {
    @RabbitListener(queues = RabbitMQConfig.GPS_QUEUE)
    public void consumeGps(String message) {
        // 1. Deserializa JSON a GpsMessage
        // 2. Llama FleetService.updateVehicleLocation()
        // 3. Registra en logs
    }
}
```

#### **FleetService.java**
```java
@Service
public class FleetService {
    private final Map<String, VehicleLocation> vehicleLocations = 
        new ConcurrentHashMap<>();
    
    public void updateVehicleLocation(GpsMessage gpsMsg) { ... }
    public FleetStatus getFleetStatus() { ... }
    public List<VehicleLocation> getAllVehicles() { ... }
}
```

#### **FleetController.java**
```java
@RestController @RequestMapping("/api/fleet")
public class FleetController {
    @GetMapping("/health") { ... }
    @GetMapping("/status") { ... }
    @GetMapping("/vehicles") { ... }
    @GetMapping("/vehicle/{id}") { ... }
    @GetMapping("/vehicle/{id}/telemetria") { ... }
}
```

---

## 🎯 Tecnologías Utilizadas

| Componente | Tecnología | Versión |
|-----------|-----------|---------|
| **IoT Sensors** | Python | 3.8+ |
| **MQTT Broker** | Mosquitto | Latest |
| **Message Broker** | RabbitMQ | 3.12 |
| **Bridge** | Python | 3.8+ |
| **Microservices** | Spring Boot | 3.4.4 |
| **JDK** | OpenJDK | 21 |
| **Frontend** | HTML5/CSS3/JavaScript | ES6 |
| **UI Framework** | Bootstrap | 5.3.0 |
| **Icons** | Font Awesome | 6.4.0 |
| **Container** | Docker | 20.10+ |
| **Orchestration** | Docker Compose | 2.0+ |

---

## 📦 Dependencias Java (Maven)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.amqp</groupId>
    <artifactId>spring-rabbit-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 🧪 Testing Manual

### 1. Verificar Conectividad
```bash
curl http://localhost:8080/api/fleet/health
# Expected: {"status":"UP","service":"Fleet API"}
```

### 2. Datos en Tiempo Real
```bash
# Ejecutar múltiples veces
for i in {1..3}; do
  curl http://localhost:8080/api/fleet/vehicles | jq ".[0] | {id: .vehicleId, speed: .speed}"
  sleep 1
done
# Expected: velocidad cambia entre consultas
```

### 3. Dashboard Web
```bash
# Abrir en navegador
http://localhost:8080

# Expected:
# - 3 tarjetas de vehículos
# - Datos actualizando cada 3 segundos
# - Colores de velocidad: verde/amarillo/rojo
```

### 4. Verificar RabbitMQ
```bash
# Acceder a Management UI
http://localhost:15672
# Login: admin/admin123

# Verificar:
# - Queue cola.gps.telemetria existe
# - Mensajes en Ready count
```

---

## 🐛 Troubleshooting

### "java.net.UnknownHostException: rabbitmq"
```bash
# Solución: Verificar variables de entorno
sudo docker compose logs fleet-api | grep SPRING_RABBITMQ_HOST

# Asegurar que docker-compose.yml tenga:
environment:
  SPRING_RABBITMQ_HOST: rabbitmq
```

### "No se actualizan los vehículos"
```bash
# Verificar que GpsConsumer está escuchando
sudo docker compose logs fleet-api | grep "GPS recibido"

# Verificar mensajes en RabbitMQ
# http://localhost:15672 → Queues → cola.gps.telemetria
```

### "Dashboard retorna 404"
```bash
# Verificar que IndexController está configurado
curl http://localhost:8080/index.html

# Debe servir el HTML completo
```

---

## 📄 Archivos de Configuración

### docker-compose.yml
- Orquesta mosquitto, rabbitmq, python-bridge, fleet-api, fleet-gps-service, fleet-alert-service, fleet-notification-service
- Red: fleet-net (bridge DNS interno)
- Volúmenes: mosquitto-data persistente

### application.yml (fleet-api)
```yaml
server:
  port: 8080
spring:
  rabbitmq:
    host: ${SPRING_RABBITMQ_HOST:localhost}
    port: ${SPRING_RABBITMQ_PORT:5672}
    username: ${SPRING_RABBITMQ_USERNAME:admin}
    password: ${SPRING_RABBITMQ_PASSWORD:admin123}
```

---

## 📋 Checklist Final

- ✅ Sensores publican a MQTT correctamente
- ✅ Bridge Python transforma MQTT → RabbitMQ
- ✅ RabbitMQ recibe mensajes en cola.gps.telemetria
- ✅ GpsConsumer deserializa mensajes
- ✅ FleetService actualiza ubicaciones
- ✅ Todos 5 endpoints REST funcionan
- ✅ Dashboard web se carga en http://localhost:8080
- ✅ Dashboard actualiza datos cada 3 segundos
- ✅ Velocidades se color-codifican (verde/amarillo/rojo)
- ✅ Estadísticas agregadas se muestran

---

**Última actualización**: 16 de mayo de 2026
**Estado**: ✅ Completo y funcional