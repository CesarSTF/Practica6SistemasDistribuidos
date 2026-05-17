import json
import sqlite3
import datetime
import paho.mqtt.client as mqtt

# Configuración del broker MQTT
BROKER = "localhost"
PUERTO = 1883

def inicializar_base_datos():
    conexion = sqlite3.connect("telemetria.db")
    cursor = conexion.cursor()

    #crear tabla si no existe
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS gps_data (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            vehicle_id TEXT,
            lat REAL,
            lng REAL,
            speed REAL,
            timestamp TEXT
        )
    ''')

    cursor.execute('''
        CREATE TABLE IF NOT EXISTS temp_data(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            vehicle_id TEXT,
            temperature REAL,
            unit TEXT,
            timestamp TEXT      
        )
''')
    
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS fuel_data(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            vehicle_id TEXT,
            fuel_level REAL,
            unit TEXT,
            timestamp TEXT      
        )
''')
    
    conexion.commit()
    conexion.close()
    print("Base de datos inicializada.")

# Callback que se ejecuta al recibir un mensaje
def al_recibir_mensaje(cliente, datos_usuario, mensaje):
    topic = mensaje.topic
    # decodificar_json(mensaje.payload)
    payload = json.loads(mensaje.payload.decode('utf-8'))

    conexion = sqlite3.connect("telemetria.db")
    cursor = conexion.cursor()

    if "/gps" in topic:
        # INSERTAR en gps_data
        cursor.execute('''
            INSERT INTO gps_data (vehicle_id, lat, lng, speed, timestamp)
            VALUES (?, ?, ?, ?, ?)
        ''', (payload["vehicle_id"], payload["lat"], payload["lng"], payload["velocidad"], payload["timestamp"]))
        
    elif "/temperatura" in topic:
        temperatura = payload["temperature"]
        # Evaluar alerta de temperatura
        if temperatura > 4:
            print(f"ALERTA: {payload['vehicle_id']} temperatura alta ({temperatura}°C)")
            
        # INSERTAR en temp_data
        cursor.execute('''
            INSERT INTO temp_data (vehicle_id, temperature, unit, timestamp)
            VALUES (?, ?, ?, ?)
        ''', (payload["vehicle_id"], payload["temperature"], payload["unit"], payload["timestamp"]))
        
    elif "/combustible" in topic:
        combustible = payload["fuel_level"]
        # Evaluar alerta de combustible
        if combustible < 20:
            print(f"ALERTA: {payload['vehicle_id']} combustible bajo ({combustible}%)")
            
        # INSERTAR en fuel_data
        cursor.execute('''
            INSERT INTO fuel_data (vehicle_id, fuel_level, unit, timestamp)
            VALUES (?, ?, ?, ?)
        ''', (payload["vehicle_id"], combustible, payload["unit"], payload["timestamp"]))

    conexion.commit()
    conexion.close()

# Función principal
def principal():
    inicializar_base_datos()

    cliente = mqtt.Client()
    cliente.on_message = al_recibir_mensaje
    cliente.connect(BROKER, PUERTO, 60)

    # suscribirse a los topicos
    cliente.subscribe("flota/+/gps")
    cliente.subscribe("flota/+/temperatura")
    cliente.subscribe("flota/+/combustible")

    print("Suscriptor activo. Escuchando topics flota/+/...")

    try:
        # INICIAR escucha continua
        cliente.loop_forever()
    except KeyboardInterrupt:
        print("\nDeteniendo suscriptor...")
        cliente.disconnect()

if __name__ == "__main__":
    principal()


