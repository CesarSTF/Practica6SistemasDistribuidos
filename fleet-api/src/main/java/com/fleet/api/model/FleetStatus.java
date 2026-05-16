package com.fleet.api.model;

import java.util.Date;

public class FleetStatus {
    private int totalVehicles;
    private int activeAlerts;
    private Date timestamp;

    public FleetStatus(int totalVehicles, int activeAlerts) {
        this.totalVehicles = totalVehicles;
        this.activeAlerts = activeAlerts;
        this.timestamp = new Date();
    }

    public int getTotalVehicles() { return totalVehicles; }
    public int getActiveAlerts() { return activeAlerts; }
    public Date getTimestamp() { return timestamp; }
}
