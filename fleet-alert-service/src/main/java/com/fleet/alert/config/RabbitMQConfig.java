package com.fleet.alert.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TEMP_ALERT_QUEUE = "cola.alertas.temperatura";
    public static final String FUEL_QUEUE = "cola.combustible.nivel";
    public static final String NOTIFICATION_QUEUE = "cola.notificaciones";
    public static final String FLEET_EXCHANGE = "exchange.fleet";

    @Bean
    public DirectExchange fleetExchange() {
        return new DirectExchange(FLEET_EXCHANGE);
    }

    @Bean
    public Queue tempAlertQueue() {
        return new Queue(TEMP_ALERT_QUEUE, true);
    }

    @Bean
    public Queue fuelQueue() {
        return new Queue(FUEL_QUEUE, true);
    }

    @Bean
    public Binding tempBinding(Queue tempAlertQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(tempAlertQueue).to(fleetExchange).with("temp.alert");
    }

    @Bean
    public Binding fuelBinding(Queue fuelQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(fuelQueue).to(fleetExchange).with("fuel.routing");
    }
}
