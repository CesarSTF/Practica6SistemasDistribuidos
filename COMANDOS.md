# Comandos

## Gestionar servicios

| Comando | Qué hace |
|---------|----------|
| `make kill` | Mata procesos en puertos 1883,5672,15672,8080-8083 |
| `make clean` | kill + elimina contenedores |
| `make up` | clean + levanta todo |
| `make start` | Arranque progresivo uno por uno |
| `make rebuild` | Reconstruye imágenes y levanta |
| `make rabbit` | Solo RabbitMQ |
| `make bridge` | RabbitMQ + bridge MQTT |
| `make api` | fleet-api |
| `make gps` | fleet-gps-service |
| `make alert` | fleet-alert-service |
| `make notif` | fleet-notification-service |
| `make ps` | Estado de los servicios |

## Probar

```bash
curl localhost:8080/api/fleet/status
curl localhost:8080/api/fleet/vehicle/VH-001/telemetria
```

## Ver logs

```bash
make logs-rabbit
make logs-bridge
```

## Cambiar a docker-compose

Editar Makefile y cambiar la primera línea a:
```
DC = docker-compose
```
