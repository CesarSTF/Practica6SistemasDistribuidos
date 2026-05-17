package com.fleet.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * API para el sistema de gestión de flotas
 * Permite enviar datos de GPS, temperatura y combustible
 */
@SpringBootApplication
public class FleetApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FleetApiApplication.class, args);
    }
}
