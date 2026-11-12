package com.aquaguard;

import com.aquaguard.model.HardwareTelemetry;
import com.aquaguard.model.SensorReading;
import com.aquaguard.service.BillingCalculationService;
import com.aquaguard.service.DemoWaterDataService;
import com.aquaguard.service.HardwareTelemetryStore;
import com.aquaguard.service.LeakageDetectionService;
import com.aquaguard.service.FirebaseTelemetryService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DemoWaterDataServiceTest {

    @Test
    void freshTelemetrySuppliesSensorReadingsAndConnectionStatus() {
        HardwareTelemetryStore store = new HardwareTelemetryStore();
        store.update(telemetry(System.currentTimeMillis(), true, true));
        DemoWaterDataService service = service(store);

        List<SensorReading> readings = service.getLatestReadings();

        assertEquals(5.2, readings.get(0).getFlowRateLpm());
        assertEquals(3.8, readings.get(1).getFlowRateLpm());
        assertEquals(125.5, readings.get(0).getTotalLitres());
        assertEquals(120L, readings.get(1).getPulseCount());
        assertTrue(readings.get(0).isValid());
        assertTrue(service.getSystemStatus().isHardwareConnected());
        assertFalse(service.getSystemStatus().isDemoMode());
        assertTrue(service.getLeakageEvaluation().getStatusLabel().contains("Insufficient Data"));
    }

    @Test
    void staleTelemetryReportsOfflineWithoutFabricatingReadings() {
        HardwareTelemetryStore store = new HardwareTelemetryStore();
        store.update(telemetry(System.currentTimeMillis() - 61_000L, true, true));
        DemoWaterDataService service = service(store);

        List<SensorReading> readings = service.getLatestReadings();

        assertTrue(readings.isEmpty());
        assertFalse(service.getSystemStatus().isHardwareConnected());
        assertTrue(service.getSystemStatus().isDemoMode());
        assertEquals("OFFLINE (Telemetry stale)", service.getSystemStatus().getWifiStatus());
    }

    @Test
    void olderTelemetryCannotReplaceTheLatestConnectionTimestamp() {
        HardwareTelemetryStore store = new HardwareTelemetryStore();
        long latestTimestamp = System.currentTimeMillis();
        assertTrue(store.update(telemetry(latestTimestamp, true, true)));
        assertFalse(store.update(telemetry(latestTimestamp - 5_000L, false, false)));
        DemoWaterDataService service = service(store);

        assertTrue(service.getSystemStatus().isHardwareConnected());
        assertEquals(latestTimestamp, service.getSystemStatus().getHardwareTelemetryTimestamp());
        assertEquals(5.2, service.getLatestReadings().get(0).getFlowRateLpm());
    }

    @Test
    void zeroFlowDoesNotMarkFreshHardwareOffline() {
        HardwareTelemetry telemetry = telemetry(System.currentTimeMillis(), true, true);
        telemetry.setInletFlowLpm(0.0);
        telemetry.setOutletFlowLpm(0.0);
        HardwareTelemetryStore store = new HardwareTelemetryStore();
        assertTrue(store.update(telemetry));

        assertTrue(service(store).getSystemStatus().isHardwareConnected());
    }

    @Test
    void unexpectedDeviceIdCannotSetHardwareConnected() {
        HardwareTelemetry telemetry = telemetry(System.currentTimeMillis(), true, true);
        telemetry.setDeviceId("ESP32-AQUAGUARD-01");
        HardwareTelemetryStore store = new HardwareTelemetryStore();

        assertFalse(store.update(telemetry));
        assertFalse(service(store).getSystemStatus().isHardwareConnected());
    }

    @Test
    void billingRequiresSamplesAroundPeriodBoundaries() {
        long monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long dayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        List<HardwareTelemetry> history = List.of(
                telemetryAt(monthStart - 5_000, 100, 100),
                telemetryAt(monthStart + 5_000, 101, 101),
                telemetryAt(weekStart - 5_000, 200, 200),
                telemetryAt(weekStart + 5_000, 201, 201),
                telemetryAt(dayStart - 5_000, 300, 300),
                telemetryAt(dayStart + 5_000, 301, 301),
                telemetryAt(System.currentTimeMillis(), 302, 302));
        FirebaseTelemetryService firebase = mock(FirebaseTelemetryService.class);
        when(firebase.getTelemetryHistory()).thenReturn(history);

        var summary = service(new HardwareTelemetryStore(), firebase).getBillingSummary();

        assertTrue(summary.isDailyAvailable());
        assertTrue(summary.isWeeklyAvailable());
        assertTrue(summary.isMonthlyAvailable());
        assertEquals(1.5, summary.getDailyConsumptionLitres());
        assertEquals(101.5, summary.getWeeklyConsumptionLitres());
        assertEquals(201.5, summary.getMonthlyConsumptionLitres());
    }

    @Test
    void staleBoundarySamplesLeaveBillingPeriodsUnavailable() {
        long now = System.currentTimeMillis();
        FirebaseTelemetryService firebase = mock(FirebaseTelemetryService.class);
        when(firebase.getTelemetryHistory()).thenReturn(List.of(
                telemetryAt(now - 120_000, 100, 100), telemetryAt(now, 101, 101)));

        var summary = service(new HardwareTelemetryStore(), firebase).getBillingSummary();

        assertFalse(summary.isDailyAvailable());
        assertFalse(summary.isWeeklyAvailable());
        assertFalse(summary.isMonthlyAvailable());
    }

    @Test
    void waterLossAggregationHandlesCounterResetsWithoutNegativeDeltas() {
        FirebaseTelemetryService firebase = mock(FirebaseTelemetryService.class);
        long now = System.currentTimeMillis();
        when(firebase.getTelemetryHistory()).thenReturn(List.of(
                telemetryAt(now - 20_000, 100, 90),
                telemetryAt(now - 10_000, 101, 91),
                telemetryAt(now, 1, 0.5)));

        var loss = service(new HardwareTelemetryStore(), firebase).getWaterLossRecords().get(0);

        assertEquals(2.0, loss.getInletVolumeLitres());
        assertEquals(1.5, loss.getOutletVolumeLitres());
        assertEquals(0.5, loss.getEstimatedLossLitres());
        assertEquals(25.0, loss.getLossPercentage());
    }

    private DemoWaterDataService service(HardwareTelemetryStore store) {
        return new DemoWaterDataService(
                new BillingCalculationService(), new LeakageDetectionService(), store);
    }

    private DemoWaterDataService service(HardwareTelemetryStore store, FirebaseTelemetryService firebase) {
        return new DemoWaterDataService(new BillingCalculationService(), new LeakageDetectionService(), store, firebase);
    }

    private HardwareTelemetry telemetryAt(long timestamp, double inletTotal, double outletTotal) {
        HardwareTelemetry telemetry = telemetry(timestamp, true, true);
        telemetry.setInletTotalLitres(inletTotal);
        telemetry.setOutletTotalLitres(outletTotal);
        return telemetry;
    }

    private HardwareTelemetry telemetry(long timestamp, boolean inletValid, boolean outletValid) {
        HardwareTelemetry telemetry = new HardwareTelemetry();
        telemetry.setDeviceId("esp32_aqua_001");
        telemetry.setInletFlowLpm(5.2);
        telemetry.setOutletFlowLpm(3.8);
        telemetry.setInletTotalLitres(125.5);
        telemetry.setOutletTotalLitres(120.0);
        telemetry.setInletPulseCount(126L);
        telemetry.setOutletPulseCount(120L);
        telemetry.setTimestamp(timestamp);
        telemetry.setInletSensorValid(inletValid);
        telemetry.setOutletSensorValid(outletValid);
        return telemetry;
    }
}