package com.fleet.api.controller;

import com.fleet.api.service.FleetService;
import com.fleet.api.model.FleetStatus;
import com.fleet.api.model.VehicleLocation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para consultar el estado del sistema de monitoreo de flota.
 * 
 * Proporciona endpoints para:
 * - Verificar salud del servicio
 * - Obtener estado agregado de la flota
 * - Consultar ubicaciones de vehículos (todos o individuales)
 * - Obtener telemetría detallada de vehículos específicos
 * 
 * <p>Rutas base: /api/fleet
 * 
 * <p>Endpoints disponibles:
 * <ul>
 *   <li>GET /api/fleet/health - Verificación de disponibilidad</li>
 *   <li>GET /api/fleet/status - Estado agregado de flota</li>
 *   <li>GET /api/fleet/vehicles - Lista de todos los vehículos</li>
 *   <li>GET /api/fleet/vehicle/{id} - Ubicación de un vehículo específico</li>
 *   <li>GET /api/fleet/vehicle/{id}/telemetria - Telemetría formateada de un vehículo</li>
 * </ul>
 * 
 * @author Sistema de Monitoreo de Flota
 * @version 1.0
 */
@RestController
@RequestMapping("/api/fleet")
public class FleetController {

    /** Servicio que gestiona el estado de la flota */
    private final FleetService fleetService;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param fleetService Servicio de gestión de flota
     */
    public FleetController(FleetService fleetService) {
        this.fleetService = fleetService;
    }

    /**
     * Verifica la disponibilidad del servicio (Health Check).
     * 
     * Endpoint usado por sistemas de monitoreo para verificar que el servicio
     * está activo y respondiendo. Usado típicamente por load balancers.
     * 
     * @return Respuesta HTTP 200 con estado "UP"
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "Fleet API"));
    }

    /**
     * Obtiene el estado agregado de toda la flota.
     * 
     * Retorna información consolidada del sistema incluyendo:
     * - Total de vehículos activos
     * - Número de alertas vigentes
     * - Timestamp de la consulta
     * 
     * @return FleetStatus con estadísticas actuales
     */
    @GetMapping("/status")
    public ResponseEntity<FleetStatus> getFleetStatus() {
        return ResponseEntity.ok(fleetService.getFleetStatus());
    }

    /**
     * Obtiene la ubicación actual de todos los vehículos.
     * 
     * Retorna array JSON con todos los vehículos que tienen datos GPS activos.
     * Usado principalmente por el dashboard web para mostrar todas las ubicaciones.
     * 
     * @return Lista de VehicleLocation con ubicaciones actuales
     */
    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleLocation>> getAllVehicles() {
        return ResponseEntity.ok(fleetService.getAllVehicles());
    }

    /**
     * Obtiene la ubicación actual de un vehículo específico.
     * 
     * Retorna null (404 Not Found) si el vehículo no existe en el sistema.
     * 
     * @param id Identificador del vehículo (ej: "VH-001")
     * @return VehicleLocation si existe, 404 Not Found si no
     */
    @GetMapping("/vehicle/{id}")
    public ResponseEntity<?> getVehicleLocation(@PathVariable String id) {
        VehicleLocation location = fleetService.getCurrentLocation(id);
        if (location == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(location);
    }

    /**
     * Obtiene telemetría formateada de un vehículo específico.
     * 
     * Retorna una lista con un string formateado con todos los datos del vehículo.
     * Útil para logging o visualización de texto.
     * 
     * @param id Identificador del vehículo (ej: "VH-001")
     * @return Lista con string de telemetría
     */
    @GetMapping("/vehicle/{id}/telemetria")
    public ResponseEntity<List<String>> getTelemetria(@PathVariable String id) {
        return ResponseEntity.ok(fleetService.getTelemetria(id));
    }
}
