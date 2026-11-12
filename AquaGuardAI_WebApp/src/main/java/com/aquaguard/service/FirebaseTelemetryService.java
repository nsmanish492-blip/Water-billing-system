package com.aquaguard.service;

import com.aquaguard.model.HardwareTelemetry;
import com.aquaguard.model.AlertItem;
import com.aquaguard.model.AppSettings;
import com.aquaguard.model.BillingSummary;
import com.aquaguard.model.MaintenanceRecord;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class FirebaseTelemetryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FirebaseTelemetryService.class);
    private static final long HISTORY_SAMPLE_INTERVAL_MS = 60_000L;

    private final FirebaseDatabase database;
    private final DatabaseReference deviceReference;
    private final DatabaseReference sensorsReference;
    private final DatabaseReference historyReference;
    private final DatabaseReference applicationReference;
    private final DatabaseReference settingsReference;
    private final DatabaseReference maintenanceReference;
    private final DatabaseReference alertsReference;
    private final DatabaseReference billingReference;
    private final HardwareTelemetryStore telemetryStore;
    private final AtomicBoolean firebaseConnected = new AtomicBoolean(false);
    private final AtomicLong lastHistorySampleTimestamp = new AtomicLong();
    private final AtomicReference<List<HardwareTelemetry>> telemetryHistory =
            new AtomicReference<>(List.of());
    private final AtomicReference<AppSettings> settings = new AtomicReference<>(new AppSettings());
    private final AtomicReference<List<MaintenanceRecord>> maintenanceRecords = new AtomicReference<>(List.of());
    private final AtomicReference<List<AlertItem>> alerts = new AtomicReference<>(List.of());
    private final AtomicReference<List<BillingSummary>> billingHistory = new AtomicReference<>(List.of());

    public FirebaseTelemetryService(FirebaseApp firebaseApp,
                                    HardwareTelemetryStore telemetryStore,
                                    @Value("${aquaguard.device.id:esp32_aqua_001}") String deviceId) {
        this.database = FirebaseDatabase.getInstance(firebaseApp);
        this.deviceReference = database.getReference("waterSystems").child(deviceId);
        this.sensorsReference = deviceReference.child("sensors");
        this.historyReference = deviceReference.child("history");
        this.applicationReference = deviceReference.child("application");
        this.settingsReference = applicationReference.child("settings");
        this.maintenanceReference = applicationReference.child("maintenance");
        this.alertsReference = applicationReference.child("alerts");
        this.billingReference = applicationReference.child("billing");
        this.telemetryStore = telemetryStore;
    }

    @PostConstruct
    public void startListeners() {
        database.getReference(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                firebaseConnected.set(Boolean.TRUE.equals(snapshot.getValue(Boolean.class)));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                firebaseConnected.set(false);
                LOGGER.warn("Firebase connection listener cancelled: {}", error.getMessage());
            }
        });

        sensorsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                HardwareTelemetry telemetry = readTelemetry(snapshot);
                if (telemetry != null && telemetryStore.update(telemetry)) {
                    long previousSample = lastHistorySampleTimestamp.get();
                    if (telemetry.getTimestamp() - previousSample >= HISTORY_SAMPLE_INTERVAL_MS
                            && lastHistorySampleTimestamp.compareAndSet(previousSample, telemetry.getTimestamp())) {
                        historyReference.child(Long.toString(telemetry.getTimestamp())).setValueAsync(telemetry);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                LOGGER.error("Firebase telemetry listener cancelled: {}", error.getMessage());
            }
        });

        historyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<HardwareTelemetry> records = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    HardwareTelemetry telemetry = child.getValue(HardwareTelemetry.class);
                    if (telemetry != null && telemetry.getTimestamp() > 0) {
                        records.add(telemetry);
                    }
                }
                records.sort(Comparator.comparingLong(HardwareTelemetry::getTimestamp));
                telemetryHistory.set(List.copyOf(records));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                LOGGER.error("Firebase history listener cancelled: {}", error.getMessage());
            }
        });

        settingsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                AppSettings stored = snapshot.getValue(AppSettings.class);
                if (stored != null) settings.set(stored);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                LOGGER.error("Firebase settings listener cancelled: {}", error.getMessage());
            }
        });

        listenToRecords(maintenanceReference, MaintenanceRecord.class,
                records -> maintenanceRecords.set(records));
        listenToRecords(alertsReference, AlertItem.class, records -> alerts.set(records));
        listenToRecords(billingReference, BillingSummary.class, records -> billingHistory.set(records));
    }

    public boolean saveTelemetry(HardwareTelemetry telemetry) {
        HardwareTelemetry latest = telemetryStore.getLatest();
        if (latest != null && telemetry.getTimestamp() < latest.getTimestamp()) {
            return false;
        }
        sensorsReference.child("sensor1").setValueAsync(sensorMap(
                telemetry.getInletFlowLpm(), telemetry.getInletTotalLitres(),
                telemetry.getInletPulseCount(), telemetry.isInletSensorValid(), telemetry.getTimestamp()));
        sensorsReference.child("sensor2").setValueAsync(sensorMap(
                telemetry.getOutletFlowLpm(), telemetry.getOutletTotalLitres(),
                telemetry.getOutletPulseCount(), telemetry.isOutletSensorValid(), telemetry.getTimestamp()));
        return true;
    }

    public List<HardwareTelemetry> getTelemetryHistory() {
        return telemetryHistory.get();
    }

    public HardwareTelemetry getLatestTelemetry() {
        return telemetryStore.getLatest();
    }

    public boolean isFirebaseConnected() {
        return firebaseConnected.get();
    }

    public AppSettings getSettings() { return settings.get(); }

    public void saveSettings(AppSettings value) {
        settings.set(value);
        settingsReference.setValueAsync(value);
    }

    public List<MaintenanceRecord> getMaintenanceRecords() { return maintenanceRecords.get(); }

    public void saveMaintenanceRecord(MaintenanceRecord record) {
        maintenanceReference.child(record.getId()).setValueAsync(record);
    }

    public List<AlertItem> getAlerts() { return alerts.get(); }

    public void saveAlert(AlertItem alert) {
        alertsReference.child(alert.getId()).setValueAsync(alert);
    }

    public void saveBillingSummary(String periodId, BillingSummary summary) {
        summary.setBillingPeriodId(periodId);
        billingReference.child(periodId).setValueAsync(summary);
    }

    public List<BillingSummary> getBillingHistory() { return billingHistory.get(); }

    private <T> void listenToRecords(DatabaseReference reference, Class<T> type,
                                     java.util.function.Consumer<List<T>> update) {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<T> records = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    T value = child.getValue(type);
                    if (value != null) {
                        if (value instanceof BillingSummary summary) {
                            summary.setBillingPeriodId(child.getKey());
                        }
                        records.add(value);
                    }
                }
                update.accept(List.copyOf(records));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                LOGGER.error("Firebase record listener cancelled for {}: {}", reference.getPath(), error.getMessage());
            }
        });
    }

    private Map<String, Object> sensorMap(double flowRate, double totalLitres, Long pulseCount,
                                          boolean sensorValid, long timestamp) {
        Map<String, Object> values = new java.util.HashMap<>();
        values.put("flowRate", flowRate);
        values.put("totalLitres", totalLitres);
        values.put("sensorValid", sensorValid);
        values.put("updatedAt", timestamp);
        if (pulseCount != null) {
            values.put("pulseCount", pulseCount);
        }
        return values;
    }

    private HardwareTelemetry readTelemetry(DataSnapshot sensors) {
        DataSnapshot inlet = sensors.child("sensor1");
        DataSnapshot outlet = sensors.child("sensor2");
        Double inletFlow = readDouble(inlet.child("flowRate"));
        Double outletFlow = readDouble(outlet.child("flowRate"));
        Double inletTotal = readDouble(inlet.child("totalLitres"));
        Double outletTotal = readDouble(outlet.child("totalLitres"));
        Long inletTimestamp = readTimestamp(inlet.child("updatedAt"));
        Long outletTimestamp = readTimestamp(outlet.child("updatedAt"));
        if (inletFlow == null || outletFlow == null || inletTotal == null || outletTotal == null
                || inletTimestamp == null || outletTimestamp == null) {
            return null;
        }

        HardwareTelemetry telemetry = new HardwareTelemetry();
        telemetry.setDeviceId(HardwareTelemetryStore.EXPECTED_DEVICE_ID);
        telemetry.setInletFlowLpm(inletFlow);
        telemetry.setOutletFlowLpm(outletFlow);
        telemetry.setInletTotalLitres(inletTotal);
        telemetry.setOutletTotalLitres(outletTotal);
        Long inletPulses = readLong(inlet.child("pulseCount"));
        Long outletPulses = readLong(outlet.child("pulseCount"));
        telemetry.setInletPulseCount(inletPulses);
        telemetry.setOutletPulseCount(outletPulses);
        telemetry.setInletSensorValid(inletPulses != null
            && Boolean.TRUE.equals(inlet.child("sensorValid").getValue(Boolean.class)));
        telemetry.setOutletSensorValid(outletPulses != null
            && Boolean.TRUE.equals(outlet.child("sensorValid").getValue(Boolean.class)));
        telemetry.setTimestamp(Math.min(inletTimestamp, outletTimestamp));
        return telemetry;
    }

    private Double readDouble(DataSnapshot snapshot) {
        Object value = snapshot.getValue();
        if (value instanceof Number number) {
            double result = number.doubleValue();
            return Double.isFinite(result) ? result : null;
        }
        return null;
    }

    private Long readLong(DataSnapshot snapshot) {
        Object value = snapshot.getValue();
        return value instanceof Number number ? number.longValue() : null;
    }

    private Long readTimestamp(DataSnapshot snapshot) {
        Object value = snapshot.getValue();
        if (value instanceof Number number) {
            return normalizeTimestamp(number.longValue());
        }
        if (value instanceof String text) {
            try {
                return normalizeTimestamp(Long.parseLong(text));
            } catch (NumberFormatException ignored) {
                try {
                    return Instant.parse(text).toEpochMilli();
                } catch (RuntimeException ignoredInstant) {
                    try {
                        return OffsetDateTime.parse(text).toInstant().toEpochMilli();
                    } catch (RuntimeException ignoredOffset) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private Long normalizeTimestamp(long timestamp) {
        if (timestamp <= 0) {
            return null;
        }
        return timestamp < 100_000_000_000L ? timestamp * 1000L : timestamp;
    }
}