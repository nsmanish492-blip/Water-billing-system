package com.aquaguard;

import com.aquaguard.model.LeakageEvaluation;
import com.aquaguard.model.SensorReading;
import com.aquaguard.service.LeakageDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LeakageDetectionServiceTest {

    private LeakageDetectionService leakageService;

    @BeforeEach
    void setUp() {
        leakageService = new LeakageDetectionService();
    }

    @Test
    @DisplayName("Balanced flow: Difference under threshold should evaluate to NORMAL")
    void testBalancedFlow() {
        SensorReading s1 = new SensorReading("s1", "Inlet", 27, 4.5, 100.0, 45000L, true, System.currentTimeMillis(), 450.0);
        SensorReading s2 = new SensorReading("s2", "Outlet", 26, 4.4, 98.0, 44100L, true, System.currentTimeMillis(), 450.0);

        LeakageEvaluation eval = leakageService.evaluate(s1, s2, 0.40, 0, 5, "SERIES");

        assertEquals(LeakageEvaluation.Severity.NORMAL, eval.getSeverity());
        assertTrue(eval.getStatusLabel().contains("Balanced Flow"));
        assertTrue(eval.isValid());
    }

    @Test
    @DisplayName("Transient discrepancy: Difference exceeds threshold but persistence < required evaluates to WARNING")
    void testTransientDiscrepancy() {
        SensorReading s1 = new SensorReading("s1", "Inlet", 27, 5.2, 100.0, 45000L, true, System.currentTimeMillis(), 450.0);
        SensorReading s2 = new SensorReading("s2", "Outlet", 26, 3.8, 80.0, 36000L, true, System.currentTimeMillis(), 450.0);

        // delta = 1.4 L/min, persistence = 2 seconds, required = 5 seconds
        LeakageEvaluation eval = leakageService.evaluate(s1, s2, 0.40, 2, 5, "SERIES");

        assertEquals(LeakageEvaluation.Severity.WARNING, eval.getSeverity());
        assertTrue(eval.getStatusLabel().contains("Transient"));
    }

    @Test
    @DisplayName("Persistent abnormal flow: Exceeds threshold and persistence >= required evaluates to CRITICAL")
    void testPersistentAbnormalFlow() {
        SensorReading s1 = new SensorReading("s1", "Inlet", 27, 5.2, 100.0, 45000L, true, System.currentTimeMillis(), 450.0);
        SensorReading s2 = new SensorReading("s2", "Outlet", 26, 3.8, 80.0, 36000L, true, System.currentTimeMillis(), 450.0);

        // delta = 1.4 L/min, persistence = 6 seconds >= 5 seconds
        LeakageEvaluation eval = leakageService.evaluate(s1, s2, 0.40, 6, 5, "SERIES");

        assertEquals(LeakageEvaluation.Severity.CRITICAL, eval.getSeverity());
        assertTrue(eval.getStatusLabel().contains("Possible Abnormal Flow — Inspect the Pipe"));
    }

    @Test
    @DisplayName("Invalid or disconnected sensor evaluates to INSUFFICIENT_DATA")
    void testInvalidSensorReading() {
        SensorReading s1 = new SensorReading("s1", "Inlet", 27, 4.5, 100.0, 45000L, true, System.currentTimeMillis(), 450.0);
        SensorReading s2 = new SensorReading("s2", "Outlet", 26, 0.0, 0.0, 0L, false, System.currentTimeMillis(), 450.0); // invalid

        LeakageEvaluation eval = leakageService.evaluate(s1, s2, 0.40, 0, 5, "SERIES");

        assertEquals(LeakageEvaluation.Severity.INSUFFICIENT_DATA, eval.getSeverity());
        assertFalse(eval.isValid());
    }

    @Test
    @DisplayName("Parallel topology bypasses differential comparison")
    void testParallelTopology() {
        SensorReading s1 = new SensorReading("s1", "Branch 1", 27, 6.0, 100.0, 45000L, true, System.currentTimeMillis(), 450.0);
        SensorReading s2 = new SensorReading("s2", "Branch 2", 26, 2.0, 50.0, 22500L, true, System.currentTimeMillis(), 450.0);

        LeakageEvaluation eval = leakageService.evaluate(s1, s2, 0.40, 10, 5, "PARALLEL");

        assertEquals(LeakageEvaluation.Severity.NORMAL, eval.getSeverity());
        assertTrue(eval.getStatusLabel().contains("Parallel"));
    }
}
