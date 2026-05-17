package com.fleet.gps.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fleet.gps.model.GpsMessage;

@Service
public class GpsService {
    private static final Logger log = LoggerFactory.getLogger(GpsService.class);

    /*
     * Procesar datos de GPS
     * @param msg Mensaje GPS a procesar
     */
    public void processGpsData(GpsMessage msg) {
        log.info("GPS procesado | Vehiculo: {} | Posicion: ({}, {}) | Velocidad: {} km/h",
                 msg.getVehicleId(), msg.getLat(), msg.getLng(), msg.getSpeed());
    }
}
