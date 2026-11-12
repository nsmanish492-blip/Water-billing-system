
package com.aquaguard.controller;

import com.aquaguard.model.HardwareTelemetry;
import com.aquaguard.service.HardwareTelemetryStore;
import com.aquaguard.service.FirebaseTelemetryService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/hardware")
public class HardwareTelemetryController {

    private final FirebaseTelemetryService firebaseService;

    public HardwareTelemetryController(FirebaseTelemetryService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @PostMapping("/telemetry")
    public ResponseEntity<Map<String, Object>> receiveTelemetry(
            @RequestBody HardwareTelemetry telemetry) {

        if (telemetry == null ||
                telemetry.getDeviceId() == null ||
                telemetry.getDeviceId().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Invalid device ID")
            );
        }

        if (!HardwareTelemetryStore.EXPECTED_DEVICE_ID.equals(telemetry.getDeviceId())) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Unexpected device ID")
            );
        }

        boolean accepted = firebaseService.saveTelemetry(telemetry);

        return ResponseEntity.ok(
            Map.of("success", accepted, "message", accepted ? "Data queued for Firebase" : "Older telemetry ignored")
        );
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestTelemetry() {

        HardwareTelemetry latest = firebaseService.getLatestTelemetry();
        if (latest == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(latest);
    }
}
