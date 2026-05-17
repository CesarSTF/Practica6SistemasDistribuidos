#!/usr/bin/env python3
"""
SERVICIO PUENTE: MQTT → RabbitMQ
Sistema de Monitoreo de Flota Logística

PROPÓSITO:
Consumir mensajes de sensores IoT desde Mosquitto MQTT Broker.
Transformar datos según tipo (GPS, temperatura, combustible).
Publicar en RabbitMQ para procesamiento por microservicios Spring Boot.

ARQUITECTURA:
  MQTT Topics (Mosquitto)
       ↓
   [Este Script]
       ↓
  RabbitMQ Queues (AMQP)
       ↓
  Microservicios Spring Boot

FLUJO DE DATOS:
1. Sensores publican a Mosquitto:
   - flota/VH-001/gps → {vehicle_id, lat, lng, velocidad, timestamp}
   - flota/VH-001/temperatura → {vehicle_id, temperature, unit, timestamp}
   - flota/VH-001/combustible → {vehicle_id, fuel_level, unit, timestamp}

2. Este script suscriptor escucha flota/#

3. Según topic, transforma y publica en RabbitMQ:
   - /gps → cola.gps.telemetria
   - /temperatura → cola.alertas.temperatura (con validación)
   - /combustible → cola.combustible.nivel (con validación)

4. Los microservicios consumen de sus colas respectivas

DEPENDENCIAS: paho-mqtt, pika
VARIABLES DE ENTORNO: MQTT_BROKER, MQTT_PORT, RABBITMQ_HOST, RABBITMQ_USER, RABBITMQ_PASS
"""

import json
import os
import paho.mqtt.client as mqtt
import pika
import logging
from datetime import datetime

# Configurar logging
logging.basicConfig(level=logging.INFO, format='[%(asctime)s] %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# CONFIGURACIONES desde variables de entorno
MQTT_BROKER = os.getenv("MQTT_BROKER", "localhost")
MQTT_PORT = int(os.getenv("MQTT_PORT", "1883"))
RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "localhost") 
RABBITMQ_USER = os.getenv("RABBITMQ_USER", "admin")
RABBITMQ_PASS = os.getenv("RABBITMQ_PASS", "admin123")

logger.info(f"Configuración: MQTT({MQTT_BROKER}:{MQTT_PORT}), RabbitMQ({RABBITMQ_HOST})")

def configurar_rabbitmq():
    # Conectar a RabbitMQ 
    credenciales = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
    conexion = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST, credentials=credenciales))
    canal = conexion.channel()
    
    # Declarar colas (durable=True para que sobrevivan a reinicios del broker) 
    canal.queue_declare(queue="cola.gps.telemetria", durable=True)
    canal.queue_declare(queue="cola.alertas.temperatura", durable=True) 
    canal.queue_declare(queue="cola.combustible.nivel", durable=True)
    canal.queue_declare(queue="cola.notificaciones", durable=True)
    
    print("✅ Colas RabbitMQ creadas exitosamente")
    print("  - cola.gps.telemetria")
    print("  - cola.alertas.temperatura")
    print("  - cola.combustible.nivel")
    print("  - cola.notificaciones") 
    return conexion, canal 

def al_recibir_mensaje_mqtt(cliente, datos_usuario, mensaje):
    topic = mensaje.topic 
    payload = json.loads(mensaje.payload.decode('utf-8')) 
    canal = datos_usuario["rabbitmq_channel"] 
    
    if "/gps" in topic: 
        # PUBLICAR en RabbitMQ 
        canal.basic_publish(
            exchange='',
            routing_key='cola.gps.telemetria', 
            body=json.dumps(payload), 
            properties=pika.BasicProperties(delivery_mode=2) # Mensaje persistente [cite: 393]
        )
        
    elif "/temperatura" in topic: 
        if payload["temperature"] > 4: 
            alerta = { 
                "type": "TEMP_ALERT", 
                "message": "Temperatura excedida" 
            }
            alerta.update(payload) 
            
            canal.basic_publish(
                exchange='',
                routing_key='cola.alertas.temperatura', 
                body=json.dumps(alerta), 
                properties=pika.BasicProperties(delivery_mode=2)
            )
            
    elif "/combustible" in topic: 
        if payload["fuel_level"] < 20: 
            alerta = { 
                "type": "FUEL_LOW", 
                "message": "Combustible bajo" 
            }
            alerta.update(payload) 
            
            canal.basic_publish(
                exchange='',
                routing_key='cola.combustible.nivel', 
                body=json.dumps(alerta), 
                properties=pika.BasicProperties(delivery_mode=2) 
            )

def principal():
    conexion_rabbit, canal_rabbit = configurar_rabbitmq() 
    
    # Usamos VERSION1 para evitar el Deprecation Warning de paho-mqtt
    cliente = mqtt.Client(mqtt.CallbackAPIVersion.VERSION1, userdata={"rabbitmq_channel": canal_rabbit}) 
    cliente.on_message = al_recibir_mensaje_mqtt 
    
    cliente.connect(MQTT_BROKER, MQTT_PORT)
    cliente.subscribe("flota/#") # Suscripción a todos los topics de la flota 
    
    print("Bridge MQTT-RabbitMQ iniciado...") 
    
    try:
        cliente.loop_forever() 
    except KeyboardInterrupt: 
        print("\nDeteniendo bridge...")
        cliente.disconnect() 
        conexion_rabbit.close() 

if __name__ == "__main__":
    principal() 