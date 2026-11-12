package com.aquaguard.service;

import com.aquaguard.model.LeakageEvaluation;
import com.aquaguard.model.SensorReading;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service providing rule-based anomaly warning evaluation between calibrated flow sensors.
 *
 * NOTE: Comparing flow rates is only valid when sensors are placed in a series arrangement
 * (Inlet -> Pipe Segment -> Outlet). Parallel branches cannot be evaluated for leaks via
 * simple subtraction.
 */
@Service
public class LeakageDetectionService {

    public LeakageEvaluation evaluate(SensorReading s1, SensorReading s2,
                                      double thresholdLpm, int persistenceSec,
                                      int requiredPersistenceSec, String topologyMode) {
        // 1. Check topology mode
        if ("PARALLEL".equalsIgnoreCase(topologyMode)) {
            return new LeakageEvaluation(
                    LeakageEvaluation.Severity.NORMAL,
                    "Parallel Configuration (Independent Branches)",
                    "secondary",
                    0.0,
                    thresholdLpm,
                    0,
                    requiredPersistenceSec,
                    "Sensors are configured on separate parallel branches. Differential comparison is disabled because flow is distributed independently across fixtures.",
                    "PARALLEL",
                    "N/A",
                    true
            );
        }

        // 2. Validate sensor availability and freshness
        if (s1 == null || s2 == null || !s1.isValid() || !s2.isValid()) {
            return new LeakageEvaluation(
                    LeakageEvaluation.Severity.INSUFFICIENT_DATA,
                    "Insufficient Data for Leakage Analysis",
                    "secondary",
                    0.0,
                    thresholdLpm,
                    0,
                    requiredPersistenceSec,
                    "One or more flow sensors are reporting invalid or missing telemetry. Differential analysis requires both calibrated sensors to be fully operational.",
                    topologyMode,
                    "None",
                    false
            );
        }

        double flow1 = s1.getFlowRateLpm();
        double flow2 = s2.getFlowRateLpm();
        double delta = roundTwoDecimals(flow1 - flow2);

        // 3. Static / Zero Flow State
        if (flow1 < 0.15 && flow2 < 0.15) {
            return new LeakageEvaluation(
                    LeakageEvaluation.Severity.NORMAL,
                    "Static / Idle State (No Flow)",
                    "success",
                    0.0,
                    thresholdLpm,
                    0,
                    requiredPersistenceSec,
                    "Both sensors register negligible flow (< 0.15 L/min). Water lines are idle and no active consumption is occurring.",
                    topologyMode,
                    "High",
                    true
            );
        }

        // 4. Negative differential (Outlet > Inlet by more than threshold)
        if (delta < -thresholdLpm) {
            return new LeakageEvaluation(
                    LeakageEvaluation.Severity.WARNING,
                    "Sensor Reading Inversion Detected",
                    "warning",
                    delta,
                    thresholdLpm,
                    persistenceSec,
                    requiredPersistenceSec,
                    String.format("Outlet sensor (%.2f L/min) is reading higher than Inlet sensor (%.2f L/min). Verify sensor calibration factors and physical flow-direction arrows on casing.", flow2, flow1),
                    topologyMode,
                    "Moderate",
                    true
            );
        }

        // 5. Positive differential (Inlet > Outlet)
        if (delta > thresholdLpm) {
            if (persistenceSec >= requiredPersistenceSec) {
                return new LeakageEvaluation(
                        LeakageEvaluation.Severity.CRITICAL,
                        "Possible Abnormal Flow — Inspect the Pipe",
                        "danger",
                        delta,
                        thresholdLpm,
                        persistenceSec,
                        requiredPersistenceSec,
                        String.format("Inlet flow (%.2f L/min) has exceeded Outlet flow (%.2f L/min) by %.2f L/min continuously for %d seconds. Discrepancy exceeds safe threshold (%.2f L/min). Check for physical pipe breach or fitting leak.",
                                flow1, flow2, delta, persistenceSec, thresholdLpm),
                        topologyMode,
                        "Rule-Based High Probability",
                        true
                );
            } else {
                return new LeakageEvaluation(
                        LeakageEvaluation.Severity.WARNING,
                        "Transient Flow Discrepancy Observed",
                        "warning",
                        delta,
                        thresholdLpm,
                        persistenceSec,
                        requiredPersistenceSec,
                        String.format("Inlet exceeds Outlet by %.2f L/min (duration: %ds / required: %ds). Awaiting persistence confirmation to rule out air bubbles or valve opening transients.",
                                delta, persistenceSec, requiredPersistenceSec),
                        topologyMode,
                        "Monitoring",
                        true
                );
            }
        }

        // 6. Normal Balanced Flow
        return new LeakageEvaluation(
                LeakageEvaluation.Severity.NORMAL,
                "Balanced Flow Across Pipe Segment",
                "success",
                delta,
                thresholdLpm,
                0,
                requiredPersistenceSec,
                String.format("Inlet (%.2f L/min) and Outlet (%.2f L/min) flow rates match within the acceptable tolerance margin (Δ = %.2f L/min <= threshold %.2f L/min).",
                        flow1, flow2, delta, thresholdLpm),
                topologyMode,
                "High",
                true
        );
    }

    private double roundTwoDecimals(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
