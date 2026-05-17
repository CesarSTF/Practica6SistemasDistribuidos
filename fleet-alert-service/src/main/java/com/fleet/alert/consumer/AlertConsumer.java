package com.fleet.alert.consumer;

import com.fleet.alert.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


/*
 * Consumidor de alertas de temperatura y combustible
 * Reenvía las alertas a la cola de notificaciones
 */
@Component
public class AlertConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertConsumer.class);
    private final RabbitTemplate rabbitTemplate;

    public AlertConsumer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /*
     * Consume alertas de temperatura de la cola temp_alert_queue
     */
    @RabbitListener(queues = RabbitMQConfig.TEMP_ALERT_QUEUE)
    public void consumeTempAlert(String message) {
        log.warn("ALERTA TEMPERATURA: {}", message);
        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_QUEUE, message);
        log.info("Alerta de temperatura reenviada a cola.notificaciones");
    }

    /*
     * Consume alertas de combustible de la cola fuel_queue
     */
    @RabbitListener(queues = RabbitMQConfig.FUEL_QUEUE)
    public void consumeFuelAlert(String message) {
        log.warn("ALERTA COMBUSTIBLE: {}", message);
        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_QUEUE, message);
        log.info("Alerta de combustible reenviada a cola.notificaciones");
    }
}
