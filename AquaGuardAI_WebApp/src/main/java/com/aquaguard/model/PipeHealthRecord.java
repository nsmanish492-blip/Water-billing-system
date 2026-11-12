package com.aquaguard.model;

/**
 * Qualitative pipe health status based on historical flow differential events.
 */
public class PipeHealthRecord {
    private String pipeSegmentId;
    private String segmentName;
    private String qualitativeStatus; // "Stable / Normal Condition", "Minor Inconsistencies Observed", "Inspection Recommended"
    private String statusBadgeClass;   // success, warning, danger
    private int recentAbnormalReadingCount;
    private int warningCountLast30Days;
    private String flowTrendDescription;
    private String inspectionRecommendation;
    private String lastInspectionDate;
    private String integrityNote;

    public PipeHealthRecord() {}

    public PipeHealthRecord(String pipeSegmentId, String segmentName, String qualitativeStatus,
                            String statusBadgeClass, int recentAbnormalReadingCount,
                            int warningCountLast30Days, String flowTrendDescription,
                            String inspectionRecommendation, String lastInspectionDate,
                            String integrityNote) {
        this.pipeSegmentId = pipeSegmentId;
        this.segmentName = segmentName;
        this.qualitativeStatus = qualitativeStatus;
        this.statusBadgeClass = statusBadgeClass;
        this.recentAbnormalReadingCount = recentAbnormalReadingCount;
        this.warningCountLast30Days = warningCountLast30Days;
        this.flowTrendDescription = flowTrendDescription;
        this.inspectionRecommendation = inspectionRecommendation;
        this.lastInspectionDate = lastInspectionDate;
        this.integrityNote = integrityNote;
    }

    public String getPipeSegmentId() { return pipeSegmentId; }
    public void setPipeSegmentId(String pipeSegmentId) { this.pipeSegmentId = pipeSegmentId; }

    public String getSegmentName() { return segmentName; }
    public void setSegmentName(String segmentName) { this.segmentName = segmentName; }

    public String getQualitativeStatus() { return qualitativeStatus; }
    public void setQualitativeStatus(String qualitativeStatus) { this.qualitativeStatus = qualitativeStatus; }

    public String getStatusBadgeClass() { return statusBadgeClass; }
    public void setStatusBadgeClass(String statusBadgeClass) { this.statusBadgeClass = statusBadgeClass; }

    public int getRecentAbnormalReadingCount() { return recentAbnormalReadingCount; }
    public void setRecentAbnormalReadingCount(int recentAbnormalReadingCount) { this.recentAbnormalReadingCount = recentAbnormalReadingCount; }

    public int getWarningCountLast30Days() { return warningCountLast30Days; }
    public void setWarningCountLast30Days(int warningCountLast30Days) { this.warningCountLast30Days = warningCountLast30Days; }

    public String getFlowTrendDescription() { return flowTrendDescription; }
    public void setFlowTrendDescription(String flowTrendDescription) { this.flowTrendDescription = flowTrendDescription; }

    public String getInspectionRecommendation() { return inspectionRecommendation; }
    public void setInspectionRecommendation(String inspectionRecommendation) { this.inspectionRecommendation = inspectionRecommendation; }

    public String getLastInspectionDate() { return lastInspectionDate; }
    public void setLastInspectionDate(String lastInspectionDate) { this.lastInspectionDate = lastInspectionDate; }

    public String getIntegrityNote() { return integrityNote; }
    public void setIntegrityNote(String integrityNote) { this.integrityNote = integrityNote; }
}
