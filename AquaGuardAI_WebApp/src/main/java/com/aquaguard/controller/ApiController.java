package com.aquaguard.controller;

import com.aquaguard.model.LeakageEvaluation;
import com.aquaguard.model.SensorReading;
import com.aquaguard.model.SystemStatus;
import com.aquaguard.service.DemoWaterDataService;
import com.aquaguard.service.WaterDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final WaterDataService dataService;

    public ApiController(WaterDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/telemetry")
    public ResponseEntity<Map<String, Object>> getTelemetry() {
        List<SensorReading> readings = dataService.getLatestReadings();
        LeakageEvaluation leakage = dataService.getLeakageEvaluation();
        SystemStatus status = dataService.getSystemStatus();

        Map<String, Object> response = new HashMap<>();
        response.put("mode", status.isHardwareConnected() ? "LIVE_HARDWARE" : "NO_LIVE_TELEMETRY");
        response.put("demoMode", status.isDemoMode());
        response.put("hardwareConnected", status.isHardwareConnected());
        response.put("telemetryTimestamp", status.getHardwareTelemetryTimestamp());
        response.put("diagnosticMessage", status.getDiagnosticMessage());
        response.put("firebaseStatus", status.getFirebaseStatus());
        response.put("timestamp", System.currentTimeMillis());
        response.put("lastUpdateText", status.getLastUpdateTimestamp());
        response.put("readings", readings);
        response.put("leakage", leakage);
        response.put("hourlyUsage", dataService.getHourlyUsage());
        response.put("dailyUsage", dataService.getDailyUsage());
        response.put("monthlyUsage", dataService.getMonthlyUsage());

        if (readings.size() >= 2) {
            response.put("sensor1", readings.get(0));
            response.put("sensor2", readings.get(1));
        }

        if (dataService instanceof DemoWaterDataService demoService) {
            response.put("simulatingLeak", demoService.isSimulatingLeak());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/demo/toggle-leak")
    public ResponseEntity<Map<String, Object>> toggleSimulateLeak() {
        Map<String, Object> response = new HashMap<>();
        if (dataService instanceof DemoWaterDataService demoService) {
            boolean previous = demoService.toggleSimulateLeak();
            boolean current = !previous;
            response.put("success", true);
            response.put("simulatingLeak", current);
            response.put("message", current ? "Simulated leak scenario activated." : "Normal balanced flow restored.");
            return ResponseEntity.ok(response);
        }
        response.put("success", false);
        response.put("message", "Simulated leak control is only supported in demo mode.");
        return ResponseEntity.badRequest().body(response);
    }
}
