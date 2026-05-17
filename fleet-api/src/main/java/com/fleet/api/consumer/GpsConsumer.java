package com.fleet.api.consumer;

import com.fleet.api.config.RabbitMQConfig;
import com.fleet.api.model.GpsMessage;
import com.fleet.api.service.FleetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consumidor de mensajes GPS desde RabbitMQ.
 * 
 * Este componente escucha continuamente la cola cola.gps.telemetria y procesa
 * cada mensaje GPS que llega. Es responsable de:
 * 
 * <p>Funciones:
 * <ul>
 *   <li>Deserializar mensajes JSON a objetos GpsMessage</li>
 *   <li>Actualizar el servicio FleetService con la nueva ubicación</li>
 *   <li>Registrar (logging) el proceso para auditoría</li>
 *   <li>Manejar errores de forma robusta</li>
 * </ul>
 * 
 * <p>Flujo de procesamiento:
 * 1. Mensaje llega a cola.gps.telemetria
 * 2. RabbitListener lo captura automáticamente
 * 3. JSON se deserializa a GpsMessage
 * 4. FleetService.updateVehicleLocation() actualiza el estado
 * 5. Resultado se registra en logs
 * 
 * @author Sistema de Monitoreo de Flota
 * @version 1.0
 */
@Component
public class GpsConsumer {

    private static final Logger log = LoggerFactory.getLogger(GpsConsumer.class);
    
    /** Servicio que gestiona las ubicaciones de vehículos */
    private final FleetService fleetService;
    
    /** Mapper de Jackson para deserializar JSON a objetos Java */
    private final ObjectMapper objectMapper;

    /**
     * Constructor con inyección de dependencias.
     * Spring proporciona automáticamente FleetService y ObjectMapper.
     * 
     * @param fleetService Servicio de gestión de flota
     * @param objectMapper Mapper JSON a Java
     */
    public GpsConsumer(FleetService fleetService, ObjectMapper objectMapper) {
        this.fleetService = fleetService;
        this.objectMapper = objectMapper;
    }

    /**
     * Escucha y procesa mensajes GPS desde RabbitMQ.
     * 
     * Este método es llamado automáticamente por Spring cada vez que
     * un nuevo mensaje llega a la cola. Está decorado con @RabbitListener
     * que lo registra como listener de la cola especificada.
     * 
     * <p>Proceso:
     * 1. Se recibe el mensaje como String JSON
     * 2. Se deserializa a GpsMessage con validación de campos
     * 3. Se llama a FleetService para actualizar ubicación
     * 4. Se registra resultado en logs
     * 
     * <p>Manejo de errores:
     * - Si deserialización falla: Se registra error pero no se lanza excepción
     * - El mensaje se considera procesado (no se reintenta)
     * 
     * @param message String JSON con datos GPS del vehículo
     */
    @RabbitListener(queues = RabbitMQConfig.GPS_QUEUE)
    public void consumeGps(String message) {
        try {
            log.info("📍 GPS recibido: {}", message);
            
            // Deserializar JSON a objeto GpsMessage
            GpsMessage gpsMsg = objectMapper.readValue(message, GpsMessage.class);
            
            // Actualizar ubicación en el servicio
            fleetService.updateVehicleLocation(gpsMsg);
            
            log.info("✅ GPS procesado: {}", gpsMsg);
        } catch (Exception e) {
            log.error("❌ Error procesando GPS: {}", e.getMessage());
        }
    }
}
