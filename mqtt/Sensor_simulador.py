import json
import time
import random
import math
import datetime
import paho.mqtt.client as mqtt

#confi
BROKER = "localhost"
PUERTO = 1883
VEHICULOS = ["VH-001", "VH-002", "VH-003"]

def on_conect(cliente, datos_usuario, flags, codigo_respuesta):
    print(f"[{datetime.datetime.now().isoformat()}] Conectado al broker MQTT (rc={codigo_respuesta})")

def simular_gps(id_vehiculo):
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
    temperatura = random.uniform(-5,8)

    return{
        "temperature": round(temperatura, 1),
        "unit": "celcius"
    }

def simular_combustible(id_vehiculo):
    combustible = random.uniform(10,100)

    return{
        "fuel_level": round(combustible, 1),
        "unit": "percent"
    }

def principal():
    cliente = mqtt.Client()
    cliente.on_connect = on_conect

    cliente.connect(BROKER, PUERTO, 60)
    cliente.loop_start()

    print("INICIANDO SIMULADOR DE SENSORES IOT...")

    try:
        while True:
            for vehiculo in VEHICULOS:
                timestamp = datetime.datetime.now().isoformat()
                
                # // -------- GPS 
                gps = simular_gps(vehiculo) 
                
                # Así se hace el "combinar" en Python 
                datos_gps = {
                    "vehicle_id": vehiculo, 
                    "timestamp": timestamp 
                }
                datos_gps.update(gps) # Combinamos con el diccionario gps 
                
                # PUBLICAR en "flota/{vehiculo}/gps" los datos_gps
                cliente.publish(f"flota/{vehiculo}/gps", json.dumps(datos_gps))
                
                # // -------- Temperatura 
                temp = simular_temperatura(vehiculo) 
                datos_temp = {
                    "vehicle_id": vehiculo, 
                    "timestamp": timestamp 
                }
                datos_temp.update(temp) 
                cliente.publish(f"flota/{vehiculo}/temperatura", json.dumps(datos_temp)) 
                
                fuel = simular_combustible(vehiculo) 
                datos_fuel = {
                    "vehicle_id": vehiculo, 
                    "timestamp": timestamp 
                }
                datos_fuel.update(fuel) 
                cliente.publish(f"flota/{vehiculo}/combustible", json.dumps(datos_fuel)) 

                print(f"[{timestamp}] Datos publicados para {vehiculo}") 
                time.sleep(5) # ESPERAR 5 segundos
                
    except KeyboardInterrupt: # CAPTURAR interrupción (Ctrl+C) 
        print("\nDeteniendo simulador...") 
        cliente.loop_stop()  
        cliente.disconnect()  

if __name__ == "__main__":
    principal()
