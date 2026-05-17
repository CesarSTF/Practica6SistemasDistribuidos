package com.fleet.api.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para la recepción de telemetría GPS.
 * 
 * Esta clase define la infraestructura AMQP necesaria para consumir mensajes
 * GPS desde la cola compartida del sistema distribuido. Establece:
 * 
 * <p>Topología AMQP:
 * <ul>
 *   <li>DirectExchange: exchange.fleet - Enrutamiento de mensajes por routing key</li>
 *   <li>Queue: cola.gps.telemetria - Cola durable para datos GPS</li>
 *   <li>Binding: Conecta la cola al exchange con routing key 'gps.routing'</li>
 * </ul>
 * 
 * <p>Flujo:
 * Python Bridge -> RabbitMQ (exchange.fleet) -> routing key 'gps.routing' 
 * -> cola.gps.telemetria -> GpsConsumer @RabbitListener
 * 
 * @author Sistema de Monitoreo de Flota
 * @version 1.0
 */
@Configuration
public class RabbitMQConfig {

    /** Nombre de la cola donde se reciben todos los mensajes GPS */
    public static final String GPS_QUEUE = "cola.gps.telemetria";
    
    /** Nombre del exchange donde se publican los mensajes */
    public static final String FLEET_EXCHANGE = "exchange.fleet";

    /**
     * Define el DirectExchange de RabbitMQ para mensajes de flota.
     * 
     * DirectExchange enruta mensajes basándose en routing keys exactas.
     * Los mensajes se envían al exchange con una routing key, y solo llegan
     * a las colas que tienen un binding con esa routing key.
     * 
     * @return DirectExchange configurado con durabilidad
     */
    @Bean
    public DirectExchange fleetExchange() {
        return new DirectExchange(FLEET_EXCHANGE);
    }

    /**
     * Define la cola de mensajes GPS.
     * 
     * La cola es durable (true), lo que significa que sobrevive a reinicios
     * de RabbitMQ. Los mensajes se almacenan hasta que se procesen.
     * 
     * @return Queue configurada como cola durable
     */
    @Bean
    public Queue gpsQueue() {
        return new Queue(GPS_QUEUE, true);
    }

    /**
     * Vincula la cola al exchange con una routing key específica.
     * 
     * Cuando un mensaje llega a exchange.fleet con routing key 'gps.routing',
     * se enruta automáticamente a cola.gps.telemetria.
     * 
     * @param gpsQueue La cola a vincular
     * @param fleetExchange El exchange a vincular
     * @return Binding que conecta queue, exchange y routing key
     */
    @Bean
    public Binding gpsBinding(Queue gpsQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(gpsQueue).to(fleetExchange).with("gps.routing");
    }
}
