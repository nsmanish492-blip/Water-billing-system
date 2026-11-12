package com.aquaguard.service;

import com.aquaguard.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Robust in-memory demonstration implementation of WaterDataService.
 *
 * NOTE: All data is clearly labelled as DEMO / SIMULATION.
 * When real hardware is deployed, replace or complement this with FirebaseWaterDataService.
 */
@Service
public class DemoWaterDataService implements WaterDataService {

    // Telemetry older than one minute is treated as offline.
    private static final long HARDWARE_STALE_TIMEOUT_MS = 60_000L;

    private final BillingCalculationService billingService;
    private final LeakageDetectionService leakageService;
    private final HardwareTelemetryStore telemetryStore;
    private final FirebaseTelemetryService firebaseTelemetryService;

    private volatile AppSettings settings = new AppSettings();
    private final List<AlertItem> alerts = new CopyOnWriteArrayList<>();
    private final List<MaintenanceRecord> maintenanceRecords = new CopyOnWriteArrayList<>();
    private final AtomicBoolean simulateLeakScenario = new AtomicBoolean(false);

        @Autowired
        public DemoWaterDataService(BillingCalculationService billingService,
                    LeakageDetectionService leakageService,
                    HardwareTelemetryStore telemetryStore,
                    FirebaseTelemetryService firebaseTelemetryService) {
        this.billingService = billingService;
        this.leakageService = leakageService;
        this.telemetryStore = telemetryStore;
        this.firebaseTelemetryService = firebaseTelemetryService;
    }

        public DemoWaterDataService(BillingCalculationService billingService,
                    LeakageDetectionService leakageService,
                    HardwareTelemetryStore telemetryStore) {
        this(billingService, leakageService, telemetryStore, null);
    }

    @Override
    public List<SensorReading> getLatestReadings() {
        HardwareTelemetry telemetry = getFreshHardwareTelemetry();
        if (telemetry == null) {
            return List.of();
        }
        return List.of(
                new SensorReading(
                    "sensor1", "Inlet Sensor (Flow Meter 1)", 27,
                    telemetry.getInletFlowLpm(), telemetry.getInletTotalLitres(),
                    telemetry.getInletPulseCount(), telemetry.isInletSensorValid(),
                    telemetry.getTimestamp(), settings.getSensor1Calibration()),
                new SensorReading(
                    "sensor2", "Outlet Sensor (Flow Meter 2)", 26,
                    telemetry.getOutletFlowLpm(), telemetry.getOutletTotalLitres(),
                    telemetry.getOutletPulseCount(), telemetry.isOutletSensorValid(),
                    telemetry.getTimestamp(), settings.getSensor2Calibration())
        );
    }

    @Override
    public SystemStatus getSystemStatus() {
        HardwareTelemetry telemetry = telemetryStore.getLatest();
        boolean hardwareConnected = isFreshHardwareTelemetry(telemetry);
        SystemStatus status = new SystemStatus();
        status.setHardwareConnected(hardwareConnected);
        status.setDemoMode(!hardwareConnected);
        status.setWifiStatus(hardwareConnected
            ? "CONNECTED (ESP32 telemetry)"
            : telemetry == null ? "OFFLINE (No telemetry)" : "OFFLINE (Telemetry stale)");
        status.setFirebaseStatus(firebaseTelemetryService != null && firebaseTelemetryService.isFirebaseConnected()
            ? "CONNECTED"
            : "DISCONNECTED");
        status.setHardwareTelemetryTimestamp(telemetry == null ? 0L : telemetry.getTimestamp());
        status.setEsp32DeviceName(telemetry == null ? settings.getDeviceName() : telemetry.getDeviceId());
        status.setLastUpdateTimestamp(telemetry == null || telemetry.getTimestamp() <= 0
            ? "Never received"
            : LocalDateTime.ofEpochSecond(telemetry.getTimestamp() / 1000L, 0, java.time.ZoneOffset.systemDefault().getRules().getOffset(java.time.Instant.ofEpochMilli(telemetry.getTimestamp())))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a")));
        status.setUptime(hardwareConnected ? "Recent telemetry received" : "Demo fallback active");

        int unacked = (int) alerts.stream().filter(a -> !a.isAcknowledged()).count();
        status.setActiveAlertCount(unacked);
        status.setDiagnosticMessage(hardwareConnected
            ? "LIVE SENSOR TELEMETRY. Billing periods, historical analytics, future-risk, and pipe-health remain estimates; pressure and structural sensors are unavailable."
            : "NO LIVE TELEMETRY: readings are unavailable because Firebase data is missing or stale.");

        return status;
    }

    @Override
    public LeakageEvaluation getLeakageEvaluation() {
        List<HardwareTelemetry> history = getTelemetryHistory();
        if (history.size() < 2) {
            return leakageService.evaluate(null, null, settings.getLeakThresholdLpm(), 0,
                    settings.getPersistenceDurationSec(), settings.getTopologyMode());
        }

        HardwareTelemetry previous = history.get(history.size() - 2);
        HardwareTelemetry latest = history.get(history.size() - 1);
        long intervalMs = latest.getTimestamp() - previous.getTimestamp();
        if (intervalMs <= 0 || intervalMs > HARDWARE_STALE_TIMEOUT_MS
            || latest.getInletTotalLitres() < previous.getInletTotalLitres()
            || latest.getOutletTotalLitres() < previous.getOutletTotalLitres()) {
            return leakageService.evaluate(null, null, settings.getLeakThresholdLpm(), 0,
                settings.getPersistenceDurationSec(), settings.getTopologyMode());
        }

        double intervalMinutes = intervalMs / 60_000.0;
        double inletFlow = (latest.getInletTotalLitres() - previous.getInletTotalLitres()) / intervalMinutes;
        double outletFlow = (latest.getOutletTotalLitres() - previous.getOutletTotalLitres()) / intervalMinutes;
        List<SensorReading> readings = getLatestReadings();
        if (readings.size() < 2) {
            return leakageService.evaluate(null, null, settings.getLeakThresholdLpm(), 0,
                settings.getPersistenceDurationSec(), settings.getTopologyMode());
        }
        readings.get(0).setFlowRateLpm(inletFlow);
        readings.get(1).setFlowRateLpm(outletFlow);
        int persistence = getPersistentDifferentialSeconds(history);
        return leakageService.evaluate(
                readings.get(0),
                readings.get(1),
                settings.getLeakThresholdLpm(),
                persistence,
                settings.getPersistenceDurationSec(),
                settings.getTopologyMode()
        );
    }

    private HardwareTelemetry getFreshHardwareTelemetry() {
        HardwareTelemetry telemetry = telemetryStore.getLatest();
        return isFreshHardwareTelemetry(telemetry) ? telemetry : null;
    }

    private boolean isFreshHardwareTelemetry(HardwareTelemetry telemetry) {
        if (telemetry == null
                || !HardwareTelemetryStore.EXPECTED_DEVICE_ID.equals(telemetry.getDeviceId())
                || telemetry.getTimestamp() <= 0
                || !Double.isFinite(telemetry.getInletFlowLpm())
                || !Double.isFinite(telemetry.getOutletFlowLpm())
                || !Double.isFinite(telemetry.getInletTotalLitres())
                || !Double.isFinite(telemetry.getOutletTotalLitres())
                || telemetry.getInletFlowLpm() < 0 || telemetry.getOutletFlowLpm() < 0
                || telemetry.getInletTotalLitres() < 0 || telemetry.getOutletTotalLitres() < 0) {
            return false;
        }

        long ageMs = System.currentTimeMillis() - telemetry.getTimestamp();
        return ageMs >= 0 && ageMs <= HARDWARE_STALE_TIMEOUT_MS;
    }

    @Override
    public FutureLeakRisk getFutureRisk() {
        List<HardwareTelemetry> history = getTelemetryHistory();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        return new FutureLeakRisk(FutureLeakRisk.RiskLevel.INSUFFICIENT_DATA, 0,
            "A future-risk score is unavailable because pipe age and structural measurements are not configured.",
            history.size() + " Firebase telemetry records are available; no verified pipe-age or pressure history exists.",
            "Continue collecting telemetry and add verified pipe age and maintenance history before scoring risk.",
            timestamp, "INSUFFICIENT DATA");
    }

    @Override
    public BillingSummary getBillingSummary() {
        List<HardwareTelemetry> history = getTelemetryHistory();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate monthStart = today.withDayOfMonth(1);
        OptionalDouble daily = periodConsumption(today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), history);
        OptionalDouble weekly = periodConsumption(weekStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), history);
        OptionalDouble monthly = periodConsumption(monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), history);
        BillingSummary summary = billingService.generateBillingSummary(
                daily.orElse(0), weekly.orElse(0), monthly.orElse(0), settings.getWaterTariffPerKiloLitre());
        summary.setDailyAvailable(daily.isPresent());
        summary.setWeeklyAvailable(weekly.isPresent());
        summary.setMonthlyAvailable(monthly.isPresent());
        summary.setPeriodDataStatus(daily.isPresent() && weekly.isPresent() && monthly.isPresent()
                ? "MEASURED FROM FIREBASE HISTORY" : "INSUFFICIENT HISTORY FOR ONE OR MORE PERIODS");
        if (!history.isEmpty()) {
            summary.setCurrentConsumptionLitres(history.get(history.size() - 1).getInletTotalLitres());
        }
        if (monthly.isPresent() && firebaseTelemetryService != null) {
            firebaseTelemetryService.saveBillingSummary(YearMonth.now().toString(), summary);
        }
        return summary;
    }

    public List<BillingSummary> getBillingHistory() {
        return firebaseTelemetryService == null ? List.of() : firebaseTelemetryService.getBillingHistory();
    }

    @Override
    public List<UsageRecord> getHourlyUsage() {
        return aggregateUsage(true, System.currentTimeMillis() - 24L * 60 * 60 * 1000);
    }

    @Override
    public List<UsageRecord> getDailyUsage() {
        return aggregateUsage(false, System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000);
    }

    @Override
    public List<UsageRecord> getMonthlyUsage() {
        return aggregateUsage(false, System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);
    }

    @Override
    public List<WaterLossRecord> getWaterLossRecords() {
        List<HardwareTelemetry> history = getTelemetryHistory();
        if (history.size() < 2) return List.of();
        HardwareTelemetry first = history.get(0);
        HardwareTelemetry last = history.get(history.size() - 1);
        double inlet = 0;
        double outlet = 0;
        long observedIntervalMs = 0;
        for (int index = 1; index < history.size(); index++) {
            HardwareTelemetry previous = history.get(index - 1);
            HardwareTelemetry current = history.get(index);
            long intervalMs = current.getTimestamp() - previous.getTimestamp();
            if (intervalMs <= 0 || intervalMs > HARDWARE_STALE_TIMEOUT_MS) continue;
            inlet += counterDelta(previous.getInletTotalLitres(), current.getInletTotalLitres());
            outlet += counterDelta(previous.getOutletTotalLitres(), current.getOutletTotalLitres());
            observedIntervalMs += intervalMs;
        }
        double loss = Math.max(0, inlet - outlet);
        if (loss <= 0) return List.of();
        double durationHours = observedIntervalMs / 3_600_000.0;
        double lossRate = durationHours > 0 ? loss / (durationHours * 60) : 0;
        double tariff = settings.getWaterTariffPerKiloLitre();
        WaterLossRecord record = new WaterLossRecord(
            "LOSS-" + first.getTimestamp(), "Estimated inlet/outlet volume discrepancy",
            Instant.ofEpochMilli(last.getTimestamp()).toString(), durationHours, loss, lossRate,
            tariff, billingService.calculateLinearCost(loss, tariff), "Measured-volume estimate",
            "Pipe location unavailable", "Flow-meter difference only; not a confirmed leak or exact location.");
        record.setInletVolumeLitres(inlet);
        record.setOutletVolumeLitres(outlet);
        record.setLossPercentage(inlet > 0 ? loss / inlet * 100.0 : 0);
        return List.of(record);
    }

    @Override
    public List<PipeHealthRecord> getPipeHealthRecords() {
        List<HardwareTelemetry> history = getTelemetryHistory();
        if (history.size() < 2) return List.of();
        int abnormal = countAbnormalIntervals(history);
        return List.of(new PipeHealthRecord(
            "UNSPECIFIED-SEGMENT", "Monitored inlet-to-outlet path",
            abnormal > 0 ? "Flow discrepancy observed" : "Insufficient structural measurements",
            abnormal > 0 ? "warning" : "secondary", abnormal, 0,
            "Derived from cumulative flow counters only.",
            abnormal > 0 ? "Inspect the monitored line and verify meter calibration." : "No pressure or structural sensors are available.",
            "Unavailable", "Pipe age, pressure, and structural condition are not measured."));
    }

    @Override
    public List<AlertItem> getAlerts() {
        List<AlertItem> stored = firebaseTelemetryService == null
                ? Collections.unmodifiableList(alerts)
                : firebaseTelemetryService.getAlerts();
        List<AlertItem> result = new ArrayList<>(stored);
        if (firebaseTelemetryService == null) return result;

        if (!getSystemStatus().isHardwareConnected()) {
            String id = "TELEMETRY-OFFLINE-" + LocalDate.now();
            if (result.stream().noneMatch(alert -> id.equals(alert.getId()))) {
                AlertItem alert = new AlertItem(id, "ESP32 telemetry unavailable",
                        "No fresh Firebase telemetry has been received within 60 seconds.", "SYSTEM",
                        AlertItem.Severity.WARNING, LocalDateTime.now().toString(),
                        "Check ESP32 Wi-Fi, Firebase writes, and the device heartbeat.", false);
                firebaseTelemetryService.saveAlert(alert);
                result.add(alert);
            }
        } else {
            List<HardwareTelemetry> history = getTelemetryHistory();
            String episodeId = leakageEpisodeId(history);
            if (episodeId != null && result.stream().noneMatch(alert -> episodeId.equals(alert.getId()))) {
                AlertItem alert = new AlertItem(episodeId, "Possible abnormal water loss",
                        "Measured inlet volume exceeded outlet volume beyond the configured threshold for consecutive samples.",
                        "LEAKAGE", AlertItem.Severity.WARNING,
                        LocalDateTime.now().toString(),
                        "Inspect the measured line and verify calibration; the flow difference does not confirm a leak location.",
                        false);
                firebaseTelemetryService.saveAlert(alert);
                result.add(alert);
            }
        }
        return List.copyOf(result);
    }

    @Override
    public boolean acknowledgeAlert(String alertId) {
        List<AlertItem> currentAlerts = getAlerts();
        for (AlertItem alert : currentAlerts) {
            if (alert.getId().equalsIgnoreCase(alertId)) {
                alert.setAcknowledged(true);
                if (firebaseTelemetryService != null) {
                    firebaseTelemetryService.saveAlert(alert);
                }
                return true;
            }
        }
        return false;
    }

    private String leakageEpisodeId(List<HardwareTelemetry> history) {
        if (history.size() < 2) return null;
        int firstIndex = history.size() - 1;
        boolean found = false;
        for (int index = history.size() - 1; index > 0; index--) {
            HardwareTelemetry previous = history.get(index - 1);
            HardwareTelemetry current = history.get(index);
            long intervalMs = current.getTimestamp() - previous.getTimestamp();
            if (intervalMs <= 0 || intervalMs > HARDWARE_STALE_TIMEOUT_MS) break;
            double inletDelta = counterDelta(previous.getInletTotalLitres(), current.getInletTotalLitres());
            double outletDelta = counterDelta(previous.getOutletTotalLitres(), current.getOutletTotalLitres());
            if (inletDelta - outletDelta <= settings.getLeakThresholdLpm() * intervalMs / 60_000.0) break;
            firstIndex = index - 1;
            found = true;
        }
        return found && getPersistentDifferentialSeconds(history) >= settings.getPersistenceDurationSec()
                ? "LEAKAGE-" + history.get(firstIndex).getTimestamp()
                : null;
    }

    @Override
    public List<MaintenanceRecord> getMaintenanceRecords() {
        if (firebaseTelemetryService != null) {
            return firebaseTelemetryService.getMaintenanceRecords();
        }
        return Collections.unmodifiableList(maintenanceRecords);
    }

    @Override
    public void addMaintenanceRecord(MaintenanceRecord record) {
        if (record != null) {
            maintenanceRecords.add(0, record);
            if (firebaseTelemetryService != null) {
                firebaseTelemetryService.saveMaintenanceRecord(record);
            }
        }
    }

    @Override
    public AppSettings getSettings() {
        if (firebaseTelemetryService != null) {
            settings = firebaseTelemetryService.getSettings();
        }
        return settings;
    }

    @Override
    public void updateSettings(AppSettings newSettings) {
        if (newSettings != null) {
            this.settings = newSettings;
            if (firebaseTelemetryService != null) {
                firebaseTelemetryService.saveSettings(newSettings);
            }
        }
    }

    private List<HardwareTelemetry> getTelemetryHistory() {
        return firebaseTelemetryService == null
                ? List.of()
                : firebaseTelemetryService.getTelemetryHistory();
    }

    private OptionalDouble periodConsumption(long periodStartMs, List<HardwareTelemetry> history) {
        int baselineIndex = -1;
        for (int index = 0; index < history.size(); index++) {
            if (history.get(index).getTimestamp() < periodStartMs) baselineIndex = index;
            else break;
        }
        if (baselineIndex < 0 || baselineIndex == history.size() - 1) return OptionalDouble.empty();
        HardwareTelemetry beforeBoundary = history.get(baselineIndex);
        HardwareTelemetry afterBoundary = history.get(baselineIndex + 1);
        if (periodStartMs - beforeBoundary.getTimestamp() > HARDWARE_STALE_TIMEOUT_MS
                || afterBoundary.getTimestamp() - periodStartMs > HARDWARE_STALE_TIMEOUT_MS) {
            return OptionalDouble.empty();
        }

        double consumed = 0;
        for (int index = baselineIndex + 1; index < history.size(); index++) {
            HardwareTelemetry previous = history.get(index - 1);
            HardwareTelemetry current = history.get(index);
            if (current.getTimestamp() < periodStartMs) continue;
            double intervalConsumption = counterDelta(previous.getInletTotalLitres(), current.getInletTotalLitres());
            if (previous.getTimestamp() < periodStartMs) {
                long intervalMs = current.getTimestamp() - previous.getTimestamp();
                double inPeriodFraction = (current.getTimestamp() - periodStartMs) / (double) intervalMs;
                consumed += intervalConsumption * Math.max(0, Math.min(1, inPeriodFraction));
            } else {
                consumed += intervalConsumption;
            }
        }
        return OptionalDouble.of(consumed);
    }

    private double counterDelta(double previous, double current) {
        if (!Double.isFinite(previous) || !Double.isFinite(current) || previous < 0 || current < 0) return 0;
        return current >= previous ? current - previous : current;
    }

    private List<UsageRecord> aggregateUsage(boolean hourly, long fromTimestampMs) {
        List<HardwareTelemetry> history = getTelemetryHistory();
        if (history.size() < 2) return List.of();
        Map<String, double[]> buckets = new LinkedHashMap<>();
        ZoneId zone = ZoneId.systemDefault();
        for (int index = 1; index < history.size(); index++) {
            HardwareTelemetry previous = history.get(index - 1);
            HardwareTelemetry current = history.get(index);
            long intervalMs = current.getTimestamp() - previous.getTimestamp();
            if (current.getTimestamp() < fromTimestampMs || intervalMs <= 0) continue;

            double inletDelta = counterDelta(previous.getInletTotalLitres(), current.getInletTotalLitres());
            double outletDelta = counterDelta(previous.getOutletTotalLitres(), current.getOutletTotalLitres());
            LocalDateTime sampleTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(current.getTimestamp()), zone);
            String key = hourly ? sampleTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00"))
                    : sampleTime.toLocalDate().toString();
            double[] bucket = buckets.computeIfAbsent(key, ignored -> new double[3]);
            bucket[0] += inletDelta;
            bucket[1] += outletDelta;
            bucket[2] += intervalMs;
        }

        List<UsageRecord> records = new ArrayList<>();
        buckets.forEach((key, totals) -> {
            LocalDateTime bucketStart = hourly
                    ? LocalDateTime.parse(key, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    : LocalDate.parse(key).atStartOfDay();
            String label = hourly ? bucketStart.format(DateTimeFormatter.ofPattern("HH:00"))
                    : bucketStart.format(DateTimeFormatter.ofPattern("EEE dd MMM"));
            double hours = totals[2] / 3_600_000.0;
            double inletFlow = hours > 0 ? totals[0] / (hours * 60) : 0;
            double outletFlow = hours > 0 ? totals[1] / (hours * 60) : 0;
            boolean abnormal = totals[0] - totals[1] > settings.getLeakThresholdLpm() * hours * 60;
            records.add(new UsageRecord(key, label, bucketStart.toString(), inletFlow, outletFlow,
                    totals[0], abnormal, "Aggregated from Firebase cumulative sensor totals."));
        });
        return records;
    }

    private int countAbnormalIntervals(List<HardwareTelemetry> history) {
        int count = 0;
        for (int index = 1; index < history.size(); index++) {
            HardwareTelemetry previous = history.get(index - 1);
            HardwareTelemetry current = history.get(index);
            long intervalMs = current.getTimestamp() - previous.getTimestamp();
            if (intervalMs <= 0 || intervalMs > HARDWARE_STALE_TIMEOUT_MS) continue;
            double inletDelta = counterDelta(previous.getInletTotalLitres(), current.getInletTotalLitres());
            double outletDelta = counterDelta(previous.getOutletTotalLitres(), current.getOutletTotalLitres());
            double thresholdLitres = settings.getLeakThresholdLpm() * intervalMs / 60_000.0;
            if (inletDelta - outletDelta > thresholdLitres) count++;
        }
        return count;
    }

    private int getPersistentDifferentialSeconds(List<HardwareTelemetry> history) {
        int seconds = 0;
        for (int index = history.size() - 1; index > 0; index--) {
            HardwareTelemetry previous = history.get(index - 1);
            HardwareTelemetry current = history.get(index);
            long intervalMs = current.getTimestamp() - previous.getTimestamp();
            if (intervalMs <= 0 || intervalMs > HARDWARE_STALE_TIMEOUT_MS) break;
            double inletDelta = counterDelta(previous.getInletTotalLitres(), current.getInletTotalLitres());
            double outletDelta = counterDelta(previous.getOutletTotalLitres(), current.getOutletTotalLitres());
            if (inletDelta - outletDelta <= settings.getLeakThresholdLpm() * intervalMs / 60_000.0) break;
            seconds += (int) (intervalMs / 1000L);
        }
        return seconds;
    }

    /**
     * Helper to toggle simulated leak mode during college demo.
     */
    public boolean toggleSimulateLeak() {
        return simulateLeakScenario.getAndSet(!simulateLeakScenario.get());
    }

    public boolean isSimulatingLeak() {
        return simulateLeakScenario.get();
    }
}
