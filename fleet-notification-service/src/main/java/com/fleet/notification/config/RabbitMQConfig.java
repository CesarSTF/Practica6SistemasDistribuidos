package com.fleet.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * Configuración de RabbitMQ para el servicio de notificaciones
 * Define la cola de notificaciones
 */
@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATION_QUEUE = "cola.notificaciones";

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }
}
