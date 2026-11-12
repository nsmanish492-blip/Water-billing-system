package com.aquaguard.service;

import com.aquaguard.model.HardwareTelemetry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class HardwareTelemetryStore {

    public static final String EXPECTED_DEVICE_ID = "esp32_aqua_001";

    private final AtomicReference<HardwareTelemetry> latestTelemetry =
            new AtomicReference<>();

    public boolean update(HardwareTelemetry telemetry) {
        if (telemetry == null || !EXPECTED_DEVICE_ID.equals(telemetry.getDeviceId())) {
            return false;
        }

        while (true) {
            HardwareTelemetry current = latestTelemetry.get();
            if (current != null && telemetry.getTimestamp() < current.getTimestamp()) {
                return false;
            }
            if (latestTelemetry.compareAndSet(current, telemetry)) {
                return true;
            }
        }
    }

    public HardwareTelemetry getLatest() {
        return latestTelemetry.get();
    }

    public boolean hasData() {
        return latestTelemetry.get() != null;
    }
}
