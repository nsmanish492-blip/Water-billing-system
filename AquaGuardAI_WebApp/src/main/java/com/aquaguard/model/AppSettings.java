package com.aquaguard.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Application configuration settings with Bean Validation constraints.
 */
public class AppSettings {

    @NotNull(message = "Tariff must be specified")
    @DecimalMin(value = "0.1", message = "Tariff must be at least ₹0.10 per 1,000 litres")
    private Double waterTariffPerKiloLitre = 15.0;

    @NotNull(message = "Sensor 1 calibration constant must be specified")
    @DecimalMin(value = "50.0", message = "Calibration factor must be at least 50 pulses/L")
    private Double sensor1Calibration = 450.0;

    @NotNull(message = "Sensor 2 calibration constant must be specified")
    @DecimalMin(value = "50.0", message = "Calibration factor must be at least 50 pulses/L")
    private Double sensor2Calibration = 450.0;

    @NotNull(message = "Leakage threshold must be specified")
    @DecimalMin(value = "0.05", message = "Leak threshold must be at least 0.05 L/min")
    private Double leakThresholdLpm = 0.40;

    @NotNull(message = "Persistence duration must be specified")
    @Min(value = 1, message = "Persistence duration must be at least 1 second")
    private Integer persistenceDurationSec = 5;

    @NotBlank(message = "Expected water-use hours must not be blank")
    private String expectedUsageHours = "06:00 - 22:00";

    @NotBlank(message = "Device name must not be blank")
    private String deviceName = "AquaGuard-ESP32-Node-01";

    private Boolean emailNotifications = true;

    private String nextMaintenanceDate = "2026-11-15";

    private String topologyMode = "SERIES"; // "SERIES" or "PARALLEL"

    public AppSettings() {}

    public Double getWaterTariffPerKiloLitre() { return waterTariffPerKiloLitre; }
    public void setWaterTariffPerKiloLitre(Double waterTariffPerKiloLitre) { this.waterTariffPerKiloLitre = waterTariffPerKiloLitre; }

    public Double getSensor1Calibration() { return sensor1Calibration; }
    public void setSensor1Calibration(Double sensor1Calibration) { this.sensor1Calibration = sensor1Calibration; }

    public Double getSensor2Calibration() { return sensor2Calibration; }
    public void setSensor2Calibration(Double sensor2Calibration) { this.sensor2Calibration = sensor2Calibration; }

    public Double getLeakThresholdLpm() { return leakThresholdLpm; }
    public void setLeakThresholdLpm(Double leakThresholdLpm) { this.leakThresholdLpm = leakThresholdLpm; }

    public Integer getPersistenceDurationSec() { return persistenceDurationSec; }
    public void setPersistenceDurationSec(Integer persistenceDurationSec) { this.persistenceDurationSec = persistenceDurationSec; }

    public String getExpectedUsageHours() { return expectedUsageHours; }
    public void setExpectedUsageHours(String expectedUsageHours) { this.expectedUsageHours = expectedUsageHours; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public Boolean getEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(Boolean emailNotifications) { this.emailNotifications = emailNotifications; }

    public String getNextMaintenanceDate() { return nextMaintenanceDate; }
    public void setNextMaintenanceDate(String nextMaintenanceDate) { this.nextMaintenanceDate = nextMaintenanceDate; }

    public String getTopologyMode() { return topologyMode; }
    public void setTopologyMode(String topologyMode) { this.topologyMode = topologyMode; }
}
