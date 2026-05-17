package com.fleet.alert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * Aplicación para recibir alertas de temperatura y combustible
 * Reenvía las alertas a la cola de notificaciones
 */
@SpringBootApplication
public class FleetAlertApplication {
    public static void main(String[] args) {
        SpringApplication.run(FleetAlertApplication.class, args);
    }
}
