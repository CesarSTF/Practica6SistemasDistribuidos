package com.fleet.gps.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String GPS_QUEUE = "cola.gps.telemetria";
    public static final String FLEET_EXCHANGE = "exchange.fleet";

    @Bean
    public DirectExchange fleetExchange() {
        return new DirectExchange(FLEET_EXCHANGE);
    }

    @Bean
    public Queue gpsQueue() {
        return new Queue(GPS_QUEUE, true);
    }

    @Bean
    public Binding gpsBinding(Queue gpsQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(gpsQueue).to(fleetExchange).with("gps.routing");
    }
}
