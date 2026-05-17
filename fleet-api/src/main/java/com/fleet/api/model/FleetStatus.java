package com.fleet.api.model;

import java.util.Date;

/**
 * Modelo de respuesta para el estado agregado de la flota.
 * 
 * Esta clase representa las estadísticas generales del sistema de monitoreo,
 * incluyendo el número total de vehículos activos y alertas vigentes.
 * Es retornada por el endpoint GET /api/fleet/status.
 * 
 * <p>Respuesta JSON típica:
 * <pre>
 * {
 *   "totalVehicles": 3,
 *   "activeAlerts": 0,
 *   "timestamp": "2026-05-17T01:33:06.018+00:00"
 * }
 * </pre>
 * 
 * @author Sistema de Monitoreo de Flota
 * @version 1.0
 */
public class FleetStatus {
    /** Número total de vehículos con telemetría activa */
    private int totalVehicles;
    
    /** Número de alertas activas en el sistema */
    private int activeAlerts;
    
    /** Marca de tiempo cuando se generó este estado */
    private Date timestamp;

    /**
     * Constructor para crear un nuevo estado de flota.
     * 
     * @param totalVehicles Cantidad total de vehículos en el sistema
     * @param activeAlerts Cantidad de alertas activas
     */
    public FleetStatus(int totalVehicles, int activeAlerts) {
        this.totalVehicles = totalVehicles;
        this.activeAlerts = activeAlerts;
        this.timestamp = new Date();
    }

    /**
     * Obtiene el número total de vehículos.
     * @return Cantidad de vehículos
     */
    public int getTotalVehicles() { return totalVehicles; }
    
    /**
     * Obtiene el número de alertas activas.
     * @return Cantidad de alertas
     */
    public int getActiveAlerts() { return activeAlerts; }
    
    /**
     * Obtiene la marca de tiempo de este estado.
     * @return Fecha y hora de creación
     */
    public Date getTimestamp() { return timestamp; }
}
