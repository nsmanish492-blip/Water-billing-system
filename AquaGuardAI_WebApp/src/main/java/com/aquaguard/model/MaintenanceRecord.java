package com.aquaguard.model;

/**
 * Maintenance logging and inspection scheduling record.
 */
public class MaintenanceRecord {
    private String id;
    private String date;
    private String component;
    private String actionTaken;
    private String technician;
    private String notes;
    private String nextScheduledDate;
    private String status; // Completed, Scheduled, In Progress

    public MaintenanceRecord() {}

    public MaintenanceRecord(String id, String date, String component, String actionTaken,
                             String technician, String notes, String nextScheduledDate, String status) {
        this.id = id;
        this.date = date;
        this.component = component;
        this.actionTaken = actionTaken;
        this.technician = technician;
        this.notes = notes;
        this.nextScheduledDate = nextScheduledDate;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }

    public String getTechnician() { return technician; }
    public void setTechnician(String technician) { this.technician = technician; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getNextScheduledDate() { return nextScheduledDate; }
    public void setNextScheduledDate(String nextScheduledDate) { this.nextScheduledDate = nextScheduledDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
