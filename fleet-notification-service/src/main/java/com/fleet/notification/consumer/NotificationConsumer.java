package com.fleet.notification.consumer;

import com.fleet.notification.config.RabbitMQConfig;
import com.fleet.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/*
 * Consumidor de notificaciones
 * Recibe notificaciones de la cola de notificaciones y las procesa
 */
@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consumeNotification(String message) {
        log.info("Procesando envio de alerta desde la cola...");
        notificationService.sendNotification(message);
    }
}

