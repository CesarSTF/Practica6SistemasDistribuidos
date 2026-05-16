package com.fleet.api.controller;

import com.fleet.api.service.FleetService;
import com.fleet.api.model.FleetStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/fleet")
public class FleetController {

    private final FleetService fleetService;

    public FleetController(FleetService fleetService) {
        this.fleetService = fleetService;
    }

    @GetMapping("/status")
    public ResponseEntity<FleetStatus> getFleetStatus() {
        return ResponseEntity.ok(fleetService.getFleetStatus());
    }

    @GetMapping("/vehicle/{id}/telemetria")
    public ResponseEntity<List<String>> getTelemetria(@PathVariable String id) {
        return ResponseEntity.ok(fleetService.getTelemetria(id));
    }
}
