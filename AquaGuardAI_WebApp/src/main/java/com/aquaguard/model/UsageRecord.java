package com.aquaguard.model;

/**
 * Historical time-series bucket record for analytics and graphs.
 */
public class UsageRecord {
    private String id;
    private String label; // e.g. "06:00", "Monday", "2026-10-12"
    private String timestamp;
    private double sensor1FlowLpm;
    private double sensor2FlowLpm;
    private double consumptionLitres;
    private boolean abnormalFlag;
    private String notes;

    public UsageRecord() {}

    public UsageRecord(String id, String label, String timestamp, double sensor1FlowLpm,
                       double sensor2FlowLpm, double consumptionLitres, boolean abnormalFlag, String notes) {
        this.id = id;
        this.label = label;
        this.timestamp = timestamp;
        this.sensor1FlowLpm = sensor1FlowLpm;
        this.sensor2FlowLpm = sensor2FlowLpm;
        this.consumptionLitres = consumptionLitres;
        this.abnormalFlag = abnormalFlag;
        this.notes = notes;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public double getSensor1FlowLpm() { return sensor1FlowLpm; }
    public void setSensor1FlowLpm(double sensor1FlowLpm) { this.sensor1FlowLpm = sensor1FlowLpm; }

    public double getSensor2FlowLpm() { return sensor2FlowLpm; }
    public void setSensor2FlowLpm(double sensor2FlowLpm) { this.sensor2FlowLpm = sensor2FlowLpm; }

    public double getConsumptionLitres() { return consumptionLitres; }
    public void setConsumptionLitres(double consumptionLitres) { this.consumptionLitres = consumptionLitres; }

    public boolean isAbnormalFlag() { return abnormalFlag; }
    public void setAbnormalFlag(boolean abnormalFlag) { this.abnormalFlag = abnormalFlag; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
