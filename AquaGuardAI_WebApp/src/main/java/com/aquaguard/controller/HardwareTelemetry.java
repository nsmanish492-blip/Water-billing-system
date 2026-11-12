package com.aquaguard.model;

public class HardwareTelemetry {

    private String deviceId;
    private double inletFlowLpm;
    private double outletFlowLpm;
    private double inletTotalLitres;
    private double outletTotalLitres;
    private Long inletPulseCount;
    private Long outletPulseCount;
    private long timestamp;
    private boolean inletSensorValid;
    private boolean outletSensorValid;

    public HardwareTelemetry() {
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public double getInletFlowLpm() {
        return inletFlowLpm;
    }

    public void setInletFlowLpm(double inletFlowLpm) {
        this.inletFlowLpm = inletFlowLpm;
    }

    public double getOutletFlowLpm() {
        return outletFlowLpm;
    }

    public void setOutletFlowLpm(double outletFlowLpm) {
        this.outletFlowLpm = outletFlowLpm;
    }

    public double getInletTotalLitres() {
        return inletTotalLitres;
    }

    public void setInletTotalLitres(double inletTotalLitres) {
        this.inletTotalLitres = inletTotalLitres;
    }

    public double getOutletTotalLitres() {
        return outletTotalLitres;
    }

    public void setOutletTotalLitres(double outletTotalLitres) {
        this.outletTotalLitres = outletTotalLitres;
    }

    public Long getInletPulseCount() {
        return inletPulseCount;
    }

    public void setInletPulseCount(Long inletPulseCount) {
        this.inletPulseCount = inletPulseCount;
    }

    public Long getOutletPulseCount() {
        return outletPulseCount;
    }

    public void setOutletPulseCount(Long outletPulseCount) {
        this.outletPulseCount = outletPulseCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isInletSensorValid() {
        return inletSensorValid;
    }

    public void setInletSensorValid(boolean inletSensorValid) {
        this.inletSensorValid = inletSensorValid;
    }

    public boolean isOutletSensorValid() {
        return outletSensorValid;
    }

    public void setOutletSensorValid(boolean outletSensorValid) {
        this.outletSensorValid = outletSensorValid;
    }
}