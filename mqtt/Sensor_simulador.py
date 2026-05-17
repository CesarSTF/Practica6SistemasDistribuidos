#!/usr/bin/env python3
"""
SIMULADOR DE SENSORES IoT - SIMULACIÓN DE GPS
Sistema de Monitoreo de Flota Logística

PROPÓSITO:
Simular sensores IoT de 3 vehículos logísticos.
Generar datos GPS, temperatura y combustible de forma continua.
Publicar a topics MQTT para que el bridge consuma y transfiera a RabbitMQ.

VEHÍCULOS SIMULADOS: VH-001, VH-002, VH-003
COORDENADAS BASE: Quito, Ecuador (-2.1709, -79.9224)
INTERVALO: 5 segundos entre simulaciones

TOPICS MQTT PUBLICADOS:
1. flota/{vehiculo}/gps: Tracking GPS en tiempo real
2. flota/{vehiculo}/temperatura: Monitoreo cadena de frío
3. flota/{vehiculo}/combustible: Alertas de combustible bajo
"""

import json
import time
import random
import datetime
import paho.mqtt.client as mqtt
import logging

logging.basicConfig(level=logging.INFO, format='[%(asctime)s] %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# CONFIGURACIÓN
BROKER = "localhost"
PUERTO = 1883
INTERVALO = 5
VEHICULOS = ["VH-001", "VH-002", "VH-003"]

def on_conect(cliente, datos_usuario, flags, codigo_respuesta):
    """Callback al conectarse a Mosquitto"""
    if codigo_respuesta == 0:
        logger.info(f"✅ Conectado al broker MQTT en {BROKER}:{PUERTO}")
    else:
        logger.error(f"❌ Error MQTT (código: {codigo_respuesta})")

def simular_gps(id_vehiculo):
    """
    Genera datos GPS simulados del vehículo.
    Base: Quito, Ecuador (-2.1709, -79.9224)
    Variación: ±0.01 grados (aproximadamente ±1 km)
    Velocidad: 20-80 km/h (realista para logística)
    """
    base_lat = -2.1709
    base_lng = -79.9224

    latitud = base_lat + random.uniform(-0.01, 0.01)
    longitud = base_lng + random.uniform(-0.01, 0.01)
    velocidad = random.uniform(20, 80)  

    return {
        "lat": round(latitud, 6),
        "lng": round(longitud, 6),
        "velocidad": round(velocidad, 1)
    }

def simular_temperatura(id_vehiculo):
    """
    Genera datos de temperatura simulados.
    Rango: -5 a 8°C (cadena de frío para productos refrigerados)
    Umbral de alerta: > 4°C (activa alerta en el bridge)
    """
    temperatura = random.uniform(-5,8)

    return{
        "temperature": round(temperatura, 1),
        "unit": "celcius"
    }

def simular_combustible(id_vehiculo):
    """
    Genera datos de combustible simulados.
    Rango: 10-100% del tanque
    Umbral de alerta: < 20% (activa alerta en el bridge)
    """
    combustible = random.uniform(10,100)

    return{
        "fuel_level": round(combustible, 1),
        "unit": "percent"
    }

def principal():
    """
    Función principal que simula y publica datos de 3 vehículos.
    
    Flujo:
    1. Conectar a Mosquitto
    2. Para cada vehículo, cada 5 segundos:
       a. Generar datos GPS, temperatura, combustible
       b. Envolver en estructura JSON con timestamp
       c. Publicar a los 3 topics (gps, temperatura, combustible)
    3. Repetir indefinidamente
    4. Graceful shutdown con Ctrl+C
    """
    cliente = mqtt.Client(mqtt.CallbackAPIVersion.VERSION1)
    cliente.on_connect = on_conect

    logger.info(f"🔗 Conectando a MQTT en {BROKER}:{PUERTO}...")
    cliente.connect(BROKER, PUERTO, 60)
    cliente.loop_start()

    time.sleep(1)
    logger.info("🚀 INICIANDO SIMULADOR DE SENSORES IoT...")
    logger.info(f"   Vehículos: {', '.join(VEHICULOS)}")
    logger.info(f"   Intervalo: {INTERVALO} segundos")
    logger.info(f"   Presionar Ctrl+C para detener\n")

    try:
        while True:
            for vehiculo in VEHICULOS:
                timestamp = datetime.datetime.now().isoformat()
                
                # 1. GPS: Generar y publicar
                gps = simular_gps(vehiculo) 
                datos_gps = {
                    "vehicle_id": vehiculo, 
                    "timestamp": timestamp 
                }
                datos_gps.update(gps)
                cliente.publish(f"flota/{vehiculo}/gps", json.dumps(datos_gps))
                
                # 2. TEMPERATURA: Generar y publicar
                temp = simular_temperatura(vehiculo) 
                datos_temp = {
                    "vehicle_id": vehiculo, 
                    "timestamp": timestamp 
                }
                datos_temp.update(temp) 
                cliente.publish(f"flota/{vehiculo}/temperatura", json.dumps(datos_temp)) 
                
                # 3. COMBUSTIBLE: Generar y publicar
                fuel = simular_combustible(vehiculo) 
                datos_fuel = {
                    "vehicle_id": vehiculo, 
                    "timestamp": timestamp 
                }
                datos_fuel.update(fuel) 
                cliente.publish(f"flota/{vehiculo}/combustible", json.dumps(datos_fuel)) 

                logger.info(
                    f"📤 {vehiculo}: GPS({gps['lat']:.4f}, {gps['lng']:.4f}, "
                    f"{gps['velocidad']}km/h) | Temp({temp['temperature']}°C) | Fuel({fuel['fuel_level']}%)"
                )
                time.sleep(INTERVALO)
                
    except KeyboardInterrupt:
        logger.info("\n⏹️ Deteniendo simulador...")
        cliente.loop_stop()
        cliente.disconnect()
        logger.info("✅ Simulador detenido correctamente")

if __name__ == "__main__":
    principal()
