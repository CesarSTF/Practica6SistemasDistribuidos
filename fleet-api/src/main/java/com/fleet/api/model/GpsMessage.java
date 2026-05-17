package com.fleet.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Modelo de datos para mensajes GPS recibidos desde MQTT.
 * 
 * Esta clase mapea los datos de telemetría GPS que llegan desde los sensores
 * a través del puente MQTT-RabbitMQ. Utiliza anotaciones @JsonProperty para
 * mapear los campos JSON snake_case del bridge Python a campos camelCase de Java.
 * 
 * <p>Flujo de datos:
 * Sensor GPS -> MQTT Topic -> Python Bridge -> RabbitMQ Queue -> Este modelo
 * 
 * <p>Ejemplo de mensaje JSON recibido:
 * <pre>
 * {
 *   "vehicle_id": "VH-001",
 *   "timestamp": "2026-05-16T10:30:45",
 *   "lat": -2.168715,
 *   "lng": -79.914435,
 *   "velocidad": 45.5
 * }
 * </pre>
 * 
 * @author Sistema de Monitoreo de Flota
 * @version 1.0
 */
public class GpsMessage {
    /** Identificador único del vehículo (ej: VH-001, VH-002) */
    @JsonProperty("vehicle_id")
    private String vehicleId;
    
    /** Marca de tiempo en formato ISO 8601 */
    @JsonProperty("timestamp")
    private String timestamp;
    
    /** Latitud del vehículo en coordenadas decimales */
    @JsonProperty("lat")
    private double lat;
    
    /** Longitud del vehículo en coordenadas decimales */
    @JsonProperty("lng")
    private double lng;
    
    /** Velocidad del vehículo en km/h */
    @JsonProperty("velocidad")
    private double speed;

    /** Constructor sin argumentos requerido por Jackson para deserialización */
    public GpsMessage() {}

    /**
     * Obtiene el identificador del vehículo.
     * @return ID del vehículo (ej: VH-001)
     */
    public String getVehicleId() { return vehicleId; }
    
    /**
     * Establece el identificador del vehículo.
     * @param vehicleId ID del vehículo a asignar
     */
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    
    /**
     * Obtiene la marca de tiempo del mensaje.
     * @return Timestamp en formato ISO 8601
     */
    public String getTimestamp() { return timestamp; }
    
    /**
     * Establece la marca de tiempo del mensaje.
     * @param timestamp Timestamp a asignar
     */
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    /**
     * Obtiene la latitud del vehículo.
     * @return Latitud en coordenadas decimales
     */
    public double getLat() { return lat; }
    
    /**
     * Establece la latitud del vehículo.
     * @param lat Latitud a asignar
     */
    public void setLat(double lat) { this.lat = lat; }
    
    /**
     * Obtiene la longitud del vehículo.
     * @return Longitud en coordenadas decimales
     */
    public double getLng() { return lng; }
    
    /**
     * Establece la longitud del vehículo.
     * @param lng Longitud a asignar
     */
    public void setLng(double lng) { this.lng = lng; }
    
    /**
     * Obtiene la velocidad del vehículo.
     * @return Velocidad en km/h
     */
    public double getSpeed() { return speed; }
    
    /**
     * Establece la velocidad del vehículo.
     * @param speed Velocidad a asignar (km/h)
     */
    public void setSpeed(double speed) { this.speed = speed; }

    /**
     * Representación en String del mensaje GPS para logging.
     * @return String con formato: GpsMessage{vehicleId='VH-001', ts='...', lat=-2.17, lng=-79.91, speed=45.5}
     */
    @Override
    public String toString() {
        return "GpsMessage{vehicleId='" + vehicleId + "', ts='" + timestamp +
               "', lat=" + lat + ", lng=" + lng + ", speed=" + speed + "}";
    }
}
