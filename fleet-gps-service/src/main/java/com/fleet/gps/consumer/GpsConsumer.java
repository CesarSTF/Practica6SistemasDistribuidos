package com.fleet.gps.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet.gps.config.RabbitMQConfig;
import com.fleet.gps.model.GpsMessage;
import com.fleet.gps.service.GpsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/*
 * Consumer de mensajes GPS
 * Recibe mensajes de la cola GPS_QUEUE y los procesa
 */
@Component
public class GpsConsumer {

    private static final Logger log = LoggerFactory.getLogger(GpsConsumer.class);
    private final GpsService gpsService;
    private final ObjectMapper objectMapper;

    public GpsConsumer(GpsService gpsService, ObjectMapper objectMapper) {
        this.gpsService = gpsService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.GPS_QUEUE)
    public void consumeGps(String message) {
        try {
            log.info("GPS recibido: {}", message);
            GpsMessage gpsMsg = objectMapper.readValue(message, GpsMessage.class);
            gpsService.processGpsData(gpsMsg);
        } catch (Exception e) {
            log.error("Error procesando GPS: {}", e.getMessage());
        }
    }
}
