package com.aquaguard.service;

import com.aquaguard.model.*;

import java.util.List;

/**
 * Primary domain interface for water telemetry, analytics, billing, and system diagnostics.
 *
 * Architecture Note:
 * For the present college demonstration, this interface is implemented by DemoWaterDataService.
 * In the final hardware review, a FirebaseWaterDataService (or MqttWaterDataService) can be
 * registered as the Spring primary bean without altering any Controller or View logic.
 */
public interface WaterDataService {

    /**
     * Retrieve the latest telemetry readings for both sensors.
     */
    List<SensorReading> getLatestReadings();

    /**
     * Retrieve global system connection and diagnostic status.
     */
    SystemStatus getSystemStatus();

    /**
     * Evaluate rule-based leakage status between sensors.
     */
    LeakageEvaluation getLeakageEvaluation();

    /**
     * Calculate heuristic predictive risk of future pipe failure.
     */
    FutureLeakRisk getFutureRisk();

    /**
     * Compute current consumption totals and estimated tariff billing.
     */
    BillingSummary getBillingSummary();

    /**
     * Historical 24-hour hourly flow records for analytics charts.
     */
    List<UsageRecord> getHourlyUsage();

    /**
     * Historical 7-day daily consumption records.
     */
    List<UsageRecord> getDailyUsage();

    /**
     * Historical 30-day daily consumption records.
     */
    List<UsageRecord> getMonthlyUsage();

    /**
     * Suspected water loss incident events.
     */
    List<WaterLossRecord> getWaterLossRecords();

    /**
     * Pipe infrastructure qualitative health evaluations.
     */
    List<PipeHealthRecord> getPipeHealthRecords();

    /**
     * Active and historical alerts list.
     */
    List<AlertItem> getAlerts();

    /**
     * Acknowledge an alert item by ID.
     */
    boolean acknowledgeAlert(String alertId);

    /**
     * Maintenance and pipe inspection log entries.
     */
    List<MaintenanceRecord> getMaintenanceRecords();

    /**
     * Add a new maintenance log entry.
     */
    void addMaintenanceRecord(MaintenanceRecord record);

    /**
     * Get system configuration parameters.
     */
    AppSettings getSettings();

    /**
     * Update system configuration parameters.
     */
    void updateSettings(AppSettings settings);
}
