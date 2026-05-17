package com.fleet.api.model;

import java.time.LocalDateTime;

/**
 * Representa la ubicación actual de un vehículo en el sistema.
 * 
 * Esta clase almacena el estado actual de un vehículo en memoria, incluyendo su
 * posición GPS, velocidad y la última vez que fue actualizado. Es utilizada por
 * FleetService como modelo de almacenamiento en la estructura de datos ConcurrentHashMap.
 * 
 * <p>Características:
 * - Thread-safe: Utilizada en contexto de múltiples consumidores
 * - Actualizable: El método update() permite refrescar los datos
 * - Inmutable después de construcción: Las propiedades solo se actualizan a través de update()
 * 
 * @author Sistema de Monitoreo de Flota
 * @version 1.0
 */
public class VehicleLocation {
    /** Identificador único del vehículo */
    private String vehicleId;
    
    /** Latitud actual en coordenadas decimales */
    private Double latitude;
    
    /** Longitud actual en coordenadas decimales */
    private Double longitude;
    
    /** Velocidad actual en km/h */
    private Double speed;
    
    /** Marca de tiempo de la última actualización */
    private LocalDateTime lastUpdate;

    /**
     * Constructor que inicializa una nueva ubicación de vehículo.
     * 
     * @param vehicleId Identificador del vehículo
     * @param latitude Latitud inicial
     * @param longitude Longitud inicial
     * @param speed Velocidad inicial en km/h
     */
    public VehicleLocation(String vehicleId, Double latitude, Double longitude, Double speed) {
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.lastUpdate = LocalDateTime.now();
    }

    /**
     * Obtiene el identificador del vehículo.
     * @return ID del vehículo
     */
    public String getVehicleId() { return vehicleId; }
    
    /**
     * Obtiene la latitud actual del vehículo.
     * @return Latitud en coordenadas decimales
     */
    public Double getLatitude() { return latitude; }
    
    /**
     * Obtiene la longitud actual del vehículo.
     * @return Longitud en coordenadas decimales
     */
    public Double getLongitude() { return longitude; }
    
    /**
     * Obtiene la velocidad actual del vehículo.
     * @return Velocidad en km/h
     */
    public Double getSpeed() { return speed; }
    
    /**
     * Obtiene la marca de tiempo de la última actualización.
     * @return LocalDateTime del último update
     */
    public LocalDateTime getLastUpdate() { return lastUpdate; }

    /**
     * Actualiza la ubicación y velocidad del vehículo.
     * 
     * Este método es llamado cada vez que se recibe un nuevo mensaje GPS.
     * Automáticamente actualiza el timestamp lastUpdate al momento actual.
     * 
     * <p>Sincronización: Este método es thread-safe cuando se usa dentro de
     * ConcurrentHashMap que maneja sincronización a nivel de entrada.
     * 
     * @param latitude Nueva latitud
     * @param longitude Nueva longitud
     * @param speed Nueva velocidad (km/h)
     */
    public void update(Double latitude, Double longitude, Double speed) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.lastUpdate = LocalDateTime.now();
    }
}
