package com.fleet.api.service;

import com.fleet.api.model.FleetStatus;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class FleetService {

    public FleetStatus getFleetStatus() {
        return new FleetStatus(3, 2);
    }

    public List<String> getTelemetria(String vehicleId) {
        return Collections.singletonList("Datos de telemetria para " + vehicleId);
    }
}
