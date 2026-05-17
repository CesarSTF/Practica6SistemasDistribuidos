package com.fleet.api.service;

import com.fleet.api.model.FleetStatus;
import com.fleet.api.model.GpsMessage;
import com.fleet.api.model.VehicleLocation;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio de gestión de estado de la flota de vehículos.
 * 
 * Este servicio es responsable de mantener en memoria el estado actual de todos
 * los vehículos en el sistema. Actúa como repositorio temporal que es actualizado
 * por el consumidor GPS y consultado por los endpoints REST.
 * 
 * <p>Responsabilidades:
 * <ul>
 *   <li>Almacenamiento en memoria (ConcurrentHashMap) de ubicaciones</li>
 *   <li>Actualización de vehículos con nuevos datos GPS</li>
 *   <li>Consulta de estado individual y agregado de flota</li>
 *   <li>Generación de telemetría y estadísticas</li>
 * </ul>
 * 
 * <p>Almacenamiento:
 * - ConcurrentHashMap: Thread-safe sin synchronized, óptimo para lecturas frecuentes
 * - Claves: String (vehicleId, ej: "VH-001")
 * - Valores: VehicleLocation (ubicación actual + timestamp)
 * 
 * <p>Nota sobre persistencia:
 * Los datos se pierden al reiniciar el servicio. Para producción, se debería
 * usar una base de datos (PostgreSQL, MongoDB, etc.)
 * 
 * @author Sistema de Monitoreo de Flota
 * @version 1.0
 */
@Service
public class FleetService {

    private static final Logger logger = LoggerFactory.getLogger(FleetService.class);
    
    /** 
     * Almacenamiento en memoria (thread-safe) de ubicaciones por vehículo.
     * Estructura: {"VH-001": VehicleLocation(...), "VH-002": VehicleLocation(...), ...}
     * ConcurrentHashMap permite acceso simultáneo de múltiples threads sin locks.
     */
    private final Map<String, VehicleLocation> vehicleLocations = new ConcurrentHashMap<>();

    /**
     * Obtiene el estado agregado de la flota.
     * 
     * @return FleetStatus con totalVehicles y activeAlerts
     */
    public FleetStatus getFleetStatus() {
        int totalVehicles = vehicleLocations.size();
        // TODO: Implementar detección de alertas (velocidad excesiva, zona geográfica, etc.)
        int activeAlerts = 0;
        return new FleetStatus(totalVehicles, activeAlerts);
    }

    /**
     * Obtiene telemetría formateada de un vehículo específico.
     * 
     * Retorna una lista con un único elemento: String con formato de telemetría.
     * Si el vehículo no existe, retorna mensaje "No hay datos...".
     * 
     * @param vehicleId Identificador del vehículo (ej: "VH-001")
     * @return Lista con string de telemetría formateado para logging/UI
     */
    public List<String> getTelemetria(String vehicleId) {
        if (!vehicleLocations.containsKey(vehicleId)) {
            return Collections.singletonList("No hay datos para el vehículo " + vehicleId);
        }
        VehicleLocation location = vehicleLocations.get(vehicleId);
        return Collections.singletonList("Vehículo: " + location.getVehicleId() + 
                " | Posición: (" + location.getLatitude() + ", " + location.getLongitude() + ")" +
                " | Velocidad: " + location.getSpeed() + " km/h" +
                " | Actualizado: " + location.getLastUpdate());
    }

    /**
     * Actualiza la ubicación de un vehículo con nuevos datos GPS.
     * 
     * Si el vehículo no existe, lo crea. Si existe, actualiza sus coordenadas
     * y velocidad con el timestamp actual. Llamado por GpsConsumer.
     * 
     * <p>Thread-safety: ConcurrentHashMap garantiza que la operación es atómica.
     * 
     * @param gpsMsg Mensaje GPS con nuevos datos de ubicación
     */
    public void updateVehicleLocation(GpsMessage gpsMsg) {
        String vehicleId = gpsMsg.getVehicleId();
        
        // Si no existe, crear nueva entrada con ubicación inicial
        vehicleLocations.putIfAbsent(vehicleId, 
            new VehicleLocation(vehicleId, gpsMsg.getLat(), gpsMsg.getLng(), gpsMsg.getSpeed()));
        
        // Actualizar ubicación existente con nuevos datos
        VehicleLocation current = vehicleLocations.get(vehicleId);
        current.update(gpsMsg.getLat(), gpsMsg.getLng(), gpsMsg.getSpeed());
        
        logger.info("📍 Ubicación actualizada: {} a ({}, {})", vehicleId, gpsMsg.getLat(), gpsMsg.getLng());
    }

    /**
     * Obtiene la ubicación actual de un vehículo específico.
     * 
     * @param vehicleId Identificador del vehículo
     * @return VehicleLocation si existe, null si no está en el sistema
     */
    public VehicleLocation getCurrentLocation(String vehicleId) {
        return vehicleLocations.get(vehicleId);
    }

    /**
     * Obtiene una lista de todas las ubicaciones actuales de vehículos.
     * 
     * @return Lista con VehicleLocation de todos los vehículos en el sistema
     */
    public List<VehicleLocation> getAllVehicles() {
        return new ArrayList<>(vehicleLocations.values());
    }
}
