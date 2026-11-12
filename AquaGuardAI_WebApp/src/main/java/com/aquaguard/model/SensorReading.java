package com.aquaguard.model;

/**
 * Encapsulates telemetry for an individual flow sensor.
 */
public class SensorReading {
    private String sensorId;
    private String name;
    private int gpioPin;
    private double flowRateLpm;
    private double totalLitres;
    private Long pulseCount;
    private boolean valid;
    private long lastUpdateMs;
    private double calibrationFactor;

    public SensorReading() {}

    public SensorReading(String sensorId, String name, int gpioPin, double flowRateLpm,
                         double totalLitres, Long pulseCount, boolean valid,
                         long lastUpdateMs, double calibrationFactor) {
        this.sensorId = sensorId;
        this.name = name;
        this.gpioPin = gpioPin;
        this.flowRateLpm = flowRateLpm;
        this.totalLitres = totalLitres;
        this.pulseCount = pulseCount;
        this.valid = valid;
        this.lastUpdateMs = lastUpdateMs;
        this.calibrationFactor = calibrationFactor;
    }

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getGpioPin() { return gpioPin; }
    public void setGpioPin(int gpioPin) { this.gpioPin = gpioPin; }

    public double getFlowRateLpm() { return flowRateLpm; }
    public void setFlowRateLpm(double flowRateLpm) { this.flowRateLpm = flowRateLpm; }

    public double getTotalLitres() { return totalLitres; }
    public void setTotalLitres(double totalLitres) { this.totalLitres = totalLitres; }

    public Long getPulseCount() { return pulseCount; }
    public void setPulseCount(Long pulseCount) { this.pulseCount = pulseCount; }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public long getLastUpdateMs() { return lastUpdateMs; }
    public void setLastUpdateMs(long lastUpdateMs) { this.lastUpdateMs = lastUpdateMs; }

    public double getCalibrationFactor() { return calibrationFactor; }
    public void setCalibrationFactor(double calibrationFactor) { this.calibrationFactor = calibrationFactor; }
}
