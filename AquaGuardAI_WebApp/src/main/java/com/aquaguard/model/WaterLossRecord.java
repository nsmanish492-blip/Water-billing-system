package com.aquaguard.model;

/**
 * Model representing an identified or simulated water loss event.
 */
public class WaterLossRecord {
    private String eventId;
    private String title;
    private String detectedAt;
    private double durationHours;
    private double estimatedLossLitres;
    private double lossRateLpm;
    private double assumedTariff;
    private double financialLossInr;
    private String confidenceLabel; // "Illustrative Demo Estimate"
    private String pipeSection;
    private String limitations;
    private double inletVolumeLitres;
    private double outletVolumeLitres;
    private double lossPercentage;

    public WaterLossRecord() {}

    public WaterLossRecord(String eventId, String title, String detectedAt, double durationHours,
                           double estimatedLossLitres, double lossRateLpm, double assumedTariff,
                           double financialLossInr, String confidenceLabel, String pipeSection,
                           String limitations) {
        this.eventId = eventId;
        this.title = title;
        this.detectedAt = detectedAt;
        this.durationHours = durationHours;
        this.estimatedLossLitres = estimatedLossLitres;
        this.lossRateLpm = lossRateLpm;
        this.assumedTariff = assumedTariff;
        this.financialLossInr = financialLossInr;
        this.confidenceLabel = confidenceLabel;
        this.pipeSection = pipeSection;
        this.limitations = limitations;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDetectedAt() { return detectedAt; }
    public void setDetectedAt(String detectedAt) { this.detectedAt = detectedAt; }

    public double getDurationHours() { return durationHours; }
    public void setDurationHours(double durationHours) { this.durationHours = durationHours; }

    public double getEstimatedLossLitres() { return estimatedLossLitres; }
    public void setEstimatedLossLitres(double estimatedLossLitres) { this.estimatedLossLitres = estimatedLossLitres; }

    public double getLossRateLpm() { return lossRateLpm; }
    public void setLossRateLpm(double lossRateLpm) { this.lossRateLpm = lossRateLpm; }

    public double getAssumedTariff() { return assumedTariff; }
    public void setAssumedTariff(double assumedTariff) { this.assumedTariff = assumedTariff; }

    public double getFinancialLossInr() { return financialLossInr; }
    public void setFinancialLossInr(double financialLossInr) { this.financialLossInr = financialLossInr; }

    public String getConfidenceLabel() { return confidenceLabel; }
    public void setConfidenceLabel(String confidenceLabel) { this.confidenceLabel = confidenceLabel; }

    public String getPipeSection() { return pipeSection; }
    public void setPipeSection(String pipeSection) { this.pipeSection = pipeSection; }

    public String getLimitations() { return limitations; }
    public void setLimitations(String limitations) { this.limitations = limitations; }

    public double getInletVolumeLitres() { return inletVolumeLitres; }
    public void setInletVolumeLitres(double inletVolumeLitres) { this.inletVolumeLitres = inletVolumeLitres; }
    public double getOutletVolumeLitres() { return outletVolumeLitres; }
    public void setOutletVolumeLitres(double outletVolumeLitres) { this.outletVolumeLitres = outletVolumeLitres; }
    public double getLossPercentage() { return lossPercentage; }
    public void setLossPercentage(double lossPercentage) { this.lossPercentage = lossPercentage; }
}
