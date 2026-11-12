package com.aquaguard.model;

/**
 * Rule-based future leakage risk evaluation model.
 */
public class FutureLeakRisk {
    public enum RiskLevel {
        INSUFFICIENT_DATA,
        LOW,
        MEDIUM,
        HIGH
    }

    private RiskLevel riskLevel;
    private int riskScore; // 0 - 100
    private String primaryReason;
    private String historicalSummary;
    private String suggestedAction;
    private String analysisTimestamp;
    private String dataSufficiency;
    private String modelType;

    public FutureLeakRisk() {
        this.modelType = "Rule-Based Heuristic Indicator (Baseline)";
    }

    public FutureLeakRisk(RiskLevel riskLevel, int riskScore, String primaryReason,
                          String historicalSummary, String suggestedAction,
                          String analysisTimestamp, String dataSufficiency) {
        this.riskLevel = riskLevel;
        this.riskScore = riskScore;
        this.primaryReason = primaryReason;
        this.historicalSummary = historicalSummary;
        this.suggestedAction = suggestedAction;
        this.analysisTimestamp = analysisTimestamp;
        this.dataSufficiency = dataSufficiency;
        this.modelType = "Rule-Based Heuristic Indicator (Baseline)";
    }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getPrimaryReason() { return primaryReason; }
    public void setPrimaryReason(String primaryReason) { this.primaryReason = primaryReason; }

    public String getHistoricalSummary() { return historicalSummary; }
    public void setHistoricalSummary(String historicalSummary) { this.historicalSummary = historicalSummary; }

    public String getSuggestedAction() { return suggestedAction; }
    public void setSuggestedAction(String suggestedAction) { this.suggestedAction = suggestedAction; }

    public String getAnalysisTimestamp() { return analysisTimestamp; }
    public void setAnalysisTimestamp(String analysisTimestamp) { this.analysisTimestamp = analysisTimestamp; }

    public String getDataSufficiency() { return dataSufficiency; }
    public void setDataSufficiency(String dataSufficiency) { this.dataSufficiency = dataSufficiency; }

    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }
}
