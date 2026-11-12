package com.aquaguard.model;

/**
 * Model representing an alert item with severity and acknowledgment status.
 */
public class AlertItem {
    public enum Severity {
        INFO,
        WARNING,
        DANGER
    }

    private String id;
    private String title;
    private String description;
    private String category; // LEAKAGE, FLOW, SENSOR, SYSTEM
    private Severity severity;
    private String timestamp;
    private String recommendedAction;
    private boolean acknowledged;

    public AlertItem() {}

    public AlertItem(String id, String title, String description, String category,
                     Severity severity, String timestamp, String recommendedAction,
                     boolean acknowledged) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.severity = severity;
        this.timestamp = timestamp;
        this.recommendedAction = recommendedAction;
        this.acknowledged = acknowledged;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
}
