# Guía de Desarrollo - Sistema de Monitoreo de Flota

**Referencia rápida para desarrolladores**

---

## 📂 Estructura del Proyecto

### Componentes Principales

```
fleet-api/                              ← MICROSERVICIO PRINCIPAL
├── src/main/java/com/fleet/api/
│   ├── config/RabbitMQConfig.java      ← Definición de topología AMQP
│   ├── consumer/GpsConsumer.java       ← Listener de RabbitMQ
│   ├── controller/
│   │   ├── FleetController.java        ← 5 endpoints REST
│   │   └── IndexController.java        ← Rutor "/" → index.html
│   ├── model/
│   │   ├── GpsMessage.java             ← @JsonProperty para deserialización
│   │   ├── VehicleLocation.java        ← ConcurrentHashMap value
│   │   └── FleetStatus.java            ← DTO de respuesta
│   ├── service/
│   │   └── FleetService.java           ← Lógica (ConcurrentHashMap)
│   └── FleetApiApplication.java        ← @SpringBootApplication
├── src/main/resources/
│   ├── application.yml                 ← RabbitMQ config (env vars)
│   └── static/index.html               ← Dashboard web
└── pom.xml                             ← Dependencias Maven

mqtt/                                   ← PUENTE PYTHON
├── mqtt_rabbitmq_bridge.py             ← Consumer MQTT → Publisher RabbitMQ
├── Sensor_simulador.py                 ← Publica GPS a MQTT
└── requirements.txt                    ← paho-mqtt, pika
```

---

## 🔄 Flujo de Datos

```
1. Sensor IoT (Python)
   └─→ publica a MQTT: flota/{vehicleId}/gps

2. Mosquitto MQTT Broker (Puerto 1883)
   └─→ almacena y distribuye a suscriptores

3. Python Bridge (mqtt_rabbitmq_bridge.py)
   └─→ consume topics MQTT
   └─→ transforma JSON
   └─→ publica a RabbitMQ/exchange.fleet/routing:gps.routing

4. RabbitMQ (Puerto 5672)
   └─→ DirectExchange: exchange.fleet
   └─→ Queue: cola.gps.telemetria
   └─→ Binding: gps.routing → cola.gps.telemetria

5. Fleet-API Java (Puerto 8080)
   └─→ GpsConsumer @RabbitListener(queues = RabbitMQConfig.GPS_QUEUE)
   └─→ ObjectMapper.readValue() → GpsMessage
   └─→ FleetService.updateVehicleLocation()
   └─→ ConcurrentHashMap.put() → VehicleLocation

6. REST API
   └─→ GET /api/fleet/vehicles
   └─→ retorna lista de VehicleLocation (JSON)

7. Dashboard (Fetch API cada 3s)
   └─→ renderiza tarjetas con ubicaciones actualizadas
```

---

## 🛠️ Tecnologías Clave

| Tarea | Tecnología | Configuración |
|-------|-----------|---------------|
| **MQTT** | Mosquitto | `mosquitto/config/mosquitto.conf` |
| **Message Broker** | RabbitMQ | `SPRING_RABBITMQ_HOST=rabbitmq` |
| **Java Microservice** | Spring Boot 3.4.4 | `pom.xml` |
| **HTTP Server** | Spring Web | `server.port=8080` |
| **AMQP Consumer** | Spring AMQP | `@RabbitListener` |
| **Frontend** | Bootstrap 5 | `static/index.html` |
| **JSON Processing** | Jackson | `@JsonProperty` annotations |
| **Thread-Safe Storage** | ConcurrentHashMap | `com.fleet.api.service.FleetService` |
| **Container** | Docker | `docker-compose.yml` |

---

## 🎯 Puntos Clave de Implementación

### 1. RabbitMQ Configuration (RabbitMQConfig.java)

```java
@Configuration
public class RabbitMQConfig {
    public static final String GPS_QUEUE = "cola.gps.telemetria";
    public static final String FLEET_EXCHANGE = "exchange.fleet";
    
    @Bean
    public DirectExchange fleetExchange() {
        return new DirectExchange(FLEET_EXCHANGE);
    }
    
    @Bean
    public Queue gpsQueue() {
        return new Queue(GPS_QUEUE, true);  // durable=true
    }
    
    @Bean
    public Binding gpsBinding(Queue gpsQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(gpsQueue)
            .to(fleetExchange)
            .with("gps.routing");  // routing key
    }
}
```

**Importancia**:
- Define la topología AMQP que Spring Boot crea automáticamente
- Si no existe, se crea al iniciar el servicio
- Thread-safe y reutilizable

### 2. Consumer Pattern (GpsConsumer.java)

```java
@Component
public class GpsConsumer {
    @RabbitListener(queues = RabbitMQConfig.GPS_QUEUE)
    public void consumeGps(String message) {
        // Se ejecuta automáticamente CADA VEZ que hay un mensaje
        GpsMessage gpsMsg = objectMapper.readValue(message, GpsMessage.class);
        fleetService.updateVehicleLocation(gpsMsg);
    }
}
```

**Importancia**:
- @RabbitListener es un listener reactivo (no es polling)
- Se ejecuta automáticamente sin threads manuales
- ObjectMapper maneja JSON automáticamente
- Errores se loguean pero no rompen el consumer

### 3. JSON Deserialization (GpsMessage.java)

```java
public class GpsMessage {
    @JsonProperty("vehicle_id")    // Mapea JSON: vehicle_id → vehicleId
    private String vehicleId;
    
    @JsonProperty("velocidad")    // Mapea JSON: velocidad → speed
    private double speed;
    // ... otros campos
}
```

**Importancia**:
- CRÍTICO: Sin @JsonProperty, Jackson busca exactamente "speed" en JSON
- El bridge Python envía "velocidad" (snake_case)
- Si no coinciden, deserialization falla silenciosamente

### 4. Thread-Safe Storage (FleetService.java)

```java
@Service
public class FleetService {
    // ConcurrentHashMap: NO requiere synchronized
    // Permite lecturas y escrituras simultáneas sin locks
    private final Map<String, VehicleLocation> vehicleLocations = 
        new ConcurrentHashMap<>();
    
    public void updateVehicleLocation(GpsMessage gpsMsg) {
        String vehicleId = gpsMsg.getVehicleId();
        
        // putIfAbsent: crea si no existe (atómico)
        vehicleLocations.putIfAbsent(vehicleId, 
            new VehicleLocation(vehicleId, gpsMsg.getLat(), 
                               gpsMsg.getLng(), gpsMsg.getSpeed()));
        
        // get: recupera y actualiza (thread-safe)
        VehicleLocation current = vehicleLocations.get(vehicleId);
        current.update(gpsMsg.getLat(), gpsMsg.getLng(), gpsMsg.getSpeed());
    }
}
```

**Importancia**:
- Dashboard hace GET continuo
- GpsConsumer hace PUT continuo
- Ambos simultáneamente = race conditions SIN ConcurrentHashMap
- ConcurrentHashMap evita locks costosos

### 5. Dashboard Polling (index.html JavaScript)

```javascript
const API_URL = 'http://localhost:8080/api/fleet';

async function updateDashboard() {
    const vehiclesResponse = await fetch(`${API_URL}/vehicles`);
    const vehicles = await vehiclesResponse.json();
    
    // Renderizar tarjetas con datos actualizados
    const vehiclesHTML = vehicles.map(vehicle => `
        <div class="vehicle-card">
            <div class="vehicle-id">${vehicle.vehicleId}</div>
            <div class="speed-indicator ${getSpeedClass(vehicle.speed)}">
                ${vehicle.speed.toFixed(1)} km/h
            </div>
            ...
        </div>
    `).join('');
    
    document.getElementById('vehicles-container').innerHTML = vehiclesHTML;
}

// Ejecutar cada 3 segundos
setInterval(updateDashboard, 3000);
```

**Importancia**:
- Fetch API: HTTP GET no bloqueante
- Template literals: generan HTML dinámico
- setInterval: polling cada 3000ms
- Error handling: catch mostrado en UI

---

## 🔧 Variables de Entorno (Docker)

### Inyectadas por docker-compose.yml

```yaml
# fleet-api environment
SPRING_RABBITMQ_HOST: rabbitmq              # Nombre del servicio (DNS interno)
SPRING_RABBITMQ_PORT: 5672
SPRING_RABBITMQ_USERNAME: admin
SPRING_RABBITMQ_PASSWORD: admin123

# application.yml las referencia
spring:
  rabbitmq:
    host: ${SPRING_RABBITMQ_HOST:localhost}  # Default: localhost si no está set
    username: ${SPRING_RABBITMQ_USERNAME:admin}
    password: ${SPRING_RABBITMQ_PASSWORD:admin123}
```

**Importancia**:
- Sin estas variables, los servicios buscan "localhost" (falla en Docker)
- El bridge de Docker permite resolución DNS de nombres de servicios
- Las variables se reemplazan ANTES de que Spring arranque

---

## 📡 Endpoints REST

### Base URL
```
http://localhost:8080/api/fleet
```

### Endpoints

| Método | Endpoint | Propósito | Respuesta |
|--------|----------|-----------|-----------|
| GET | `/health` | Health check | `{"status":"UP"}` |
| GET | `/status` | Estado agregado | `{"totalVehicles":3,"activeAlerts":0}` |
| GET | `/vehicles` | Todos los vehículos | `[VehicleLocation, ...]` |
| GET | `/vehicle/{id}` | Vehículo específico | `VehicleLocation` o 404 |
| GET | `/vehicle/{id}/telemetria` | Telemetría en texto | `["Vehículo: VH-001..."]` |

### Ejemplo: GET /api/fleet/vehicles

```json
[
  {
    "vehicleId": "VH-001",
    "latitude": -2.168715,
    "longitude": -79.914435,
    "speed": 77.7,
    "lastUpdate": "2026-05-17T01:33:06"
  },
  {
    "vehicleId": "VH-002",
    "latitude": -2.168724,
    "longitude": -79.914456,
    "speed": 45.2,
    "lastUpdate": "2026-05-17T01:33:05"
  }
]
```

---

## 🧪 Testing

### Test 1: Conectividad API

```bash
curl http://localhost:8080/api/fleet/health
# Response: {"status":"UP","service":"Fleet API"}
```

### Test 2: Datos en Vivo

```bash
# Ejecutar 3 veces, esperar 1s entre llamadas
for i in {1..3}; do
  echo "=== Llamada $i ==="
  curl http://localhost:8080/api/fleet/vehicles | jq ".[0] | {id, speed}"
  sleep 1
done

# Expected: velocidad cambia entre llamadas
```

### Test 3: RabbitMQ

```bash
# Acceder a Management UI
http://localhost:15672
# Usuario: admin
# Contraseña: admin123

# Verificar:
# - Queues → cola.gps.telemetria
# - Debe haber mensajes en "Ready" count (se procesan continuamente)
# - Graph muestra mensajes/segundo
```

### Test 4: Dashboard

```bash
# Abrir en navegador
http://localhost:8080

# Verificar:
# 1. Carga HTML sin errores
# 2. Muestra 3 tarjetas de vehículos
# 3. Datos se actualizan cada 3 segundos (indicador pulsante)
# 4. Velocidades tienen colores: verde (<20), amarillo (20-50), rojo (>50)
# 5. Timestamps cambian
```

---

## 🐛 Debugging Common Issues

### Problema: "java.net.UnknownHostException: rabbitmq"
**Causa**: SPRING_RABBITMQ_HOST no está configurado o es "localhost"  
**Solución**: Verificar docker-compose.yml tiene `SPRING_RABBITMQ_HOST: rabbitmq`

```bash
# Verificar
sudo docker compose logs fleet-api | grep SPRING_RABBITMQ_HOST
sudo docker compose exec fleet-api cat /proc/sys/net/ipv4/ip_forward
```

### Problema: "Error procesando GPS: null"
**Causa**: @JsonProperty names no coinciden con JSON del bridge  
**Solución**: GpsMessage debe tener:
```java
@JsonProperty("vehicle_id")   // JSON viene como vehicle_id, no vehicleId
private String vehicleId;
```

### Problema: Dashboard muestra "Cargando..." indefinidamente
**Causa**: API no responde o no devuelve JSON válido  
**Solución**: 
```bash
curl http://localhost:8080/api/fleet/vehicles
# Debe retornar array JSON válido
# Si error 404 o vacío, revisar GpsConsumer logs
sudo docker compose logs fleet-api | grep GPS
```

### Problema: RabbitMQ muestra cola vacía
**Causa**: Bridge no está conectado o sensores no publican  
**Solución**:
```bash
# Verificar bridge logs
sudo docker compose logs mqtt-bridge | grep -i error

# Verificar sensores
sudo docker compose exec mosquitto mosquitto_sub -t "flota/+/gps" -C 1
```

---

## 📋 Checklist de Deployment

- [ ] Todos los contenedores construidos: `sudo docker compose build`
- [ ] RabbitMQ inicia: `sudo docker compose up -d rabbitmq`
- [ ] RabbitMQ sano: `sudo docker compose ps` (status: healthy)
- [ ] Mosquitto inicia: `sudo docker compose up -d mosquitto`
- [ ] Bridge conecta: `sudo docker compose logs mqtt-bridge | grep -i connected`
- [ ] Fleet-API inicia: `sudo docker compose up -d fleet-api`
- [ ] API responde: `curl http://localhost:8080/api/fleet/health`
- [ ] Datos fluyen: `curl http://localhost:8080/api/fleet/vehicles | jq length` (> 0)
- [ ] Dashboard carga: `http://localhost:8080` (sin 404)
- [ ] Dashboard actualiza: Datos cambian cada 3 segundos

---

## 📚 Referencias Rápidas

### Spring Boot AMQP
- `@RabbitListener`: Escucha una cola automáticamente
- `@Component`: Registra el bean en Spring context
- `ObjectMapper`: Convierte JSON a/desde objetos Java

### RabbitMQ Concepts
- **Exchange**: Enruta mensajes basado en routing key
- **Queue**: Almacena mensajes hasta que se consuman
- **Binding**: Conecta queue con exchange + routing key
- **DirectExchange**: Enruta a queues con routing key exacta

### JavaScript/HTML
- **Fetch API**: HTTP requests no bloqueantes
- **Template literals**: String interpolation con `${}`
- **setInterval()**: Ejecuta función cada N ms
- **map().join()**: Convierte array a string HTML

---

## 🔗 Archivos Relacionados

- [README.md](README.md) - Documentación general
- [COMANDOS.md](COMANDOS.md) - Comandos útiles
- [ESPECIFICACION.md](ESPECIFICACION.md) - Especificación técnica
- [docker-compose.yml](docker-compose.yml) - Orquestación de servicios
- [pom.xml](fleet-api/pom.xml) - Dependencias Maven

---

**Última actualización**: 16 de mayo de 2026  
**Mantenedor**: Sistema de Monitoreo de Flota
