package com.aquaguard.model;

/**
 * Transparent rule-based leakage evaluation model.
 */
public class LeakageEvaluation {
    public enum Severity {
        NORMAL,
        WARNING,
        CRITICAL,
        INSUFFICIENT_DATA
    }

    private Severity severity;
    private String statusLabel;
    private String statusBadgeClass; // success, warning, danger, secondary
    private double differentialFlowLpm;
    private double thresholdLpm;
    private int persistenceSeconds;
    private int requiredPersistenceSeconds;
    private String explanation;
    private String topologyMode; // SERIES or PARALLEL
    private String confidenceLevel;
    private boolean valid;

    public LeakageEvaluation() {}

    public LeakageEvaluation(Severity severity, String statusLabel, String statusBadgeClass,
                             double differentialFlowLpm, double thresholdLpm,
                             int persistenceSeconds, int requiredPersistenceSeconds,
                             String explanation, String topologyMode,
                             String confidenceLevel, boolean valid) {
        this.severity = severity;
        this.statusLabel = statusLabel;
        this.statusBadgeClass = statusBadgeClass;
        this.differentialFlowLpm = differentialFlowLpm;
        this.thresholdLpm = thresholdLpm;
        this.persistenceSeconds = persistenceSeconds;
        this.requiredPersistenceSeconds = requiredPersistenceSeconds;
        this.explanation = explanation;
        this.topologyMode = topologyMode;
        this.confidenceLevel = confidenceLevel;
        this.valid = valid;
    }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }

    public String getStatusBadgeClass() { return statusBadgeClass; }
    public void setStatusBadgeClass(String statusBadgeClass) { this.statusBadgeClass = statusBadgeClass; }

    public double getDifferentialFlowLpm() { return differentialFlowLpm; }
    public void setDifferentialFlowLpm(double differentialFlowLpm) { this.differentialFlowLpm = differentialFlowLpm; }

    public double getThresholdLpm() { return thresholdLpm; }
    public void setThresholdLpm(double thresholdLpm) { this.thresholdLpm = thresholdLpm; }

    public int getPersistenceSeconds() { return persistenceSeconds; }
    public void setPersistenceSeconds(int persistenceSeconds) { this.persistenceSeconds = persistenceSeconds; }

    public int getRequiredPersistenceSeconds() { return requiredPersistenceSeconds; }
    public void setRequiredPersistenceSeconds(int requiredPersistenceSeconds) { this.requiredPersistenceSeconds = requiredPersistenceSeconds; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getTopologyMode() { return topologyMode; }
    public void setTopologyMode(String topologyMode) { this.topologyMode = topologyMode; }

    public String getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(String confidenceLevel) { this.confidenceLevel = confidenceLevel; }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
}
