package com.aquaguard;

import com.aquaguard.model.BillingSummary;
import com.aquaguard.service.BillingCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BillingCalculationServiceTest {

    private BillingCalculationService billingService;

    @BeforeEach
    void setUp() {
        billingService = new BillingCalculationService();
    }

    @Test
    @DisplayName("Calculate standard linear water cost: 1000 litres at ₹15 should be ₹15.00")
    void testStandardCalculation() {
        double cost = billingService.calculateLinearCost(1000.0, 15.0);
        assertEquals(15.00, cost, 0.001);
    }

    @Test
    @DisplayName("Calculate fractional volume: 450 litres at ₹15 should be ₹6.75")
    void testFractionalVolumeCalculation() {
        double cost = billingService.calculateLinearCost(450.0, 15.0);
        assertEquals(6.75, cost, 0.001);
    }

    @Test
    @DisplayName("Calculate zero volume or zero tariff should return 0.00")
    void testZeroCalculation() {
        assertEquals(0.0, billingService.calculateLinearCost(0.0, 15.0));
        assertEquals(0.0, billingService.calculateLinearCost(500.0, 0.0));
        assertEquals(0.0, billingService.calculateLinearCost(-100.0, 15.0));
    }

    @Test
    @DisplayName("Generate complete billing summary with slab tier breakdown")
    void testGenerateBillingSummary() {
        BillingSummary summary = billingService.generateBillingSummary(400.0, 2800.0, 12000.0, 15.0);
        assertNotNull(summary);
        assertEquals(15.00, summary.getTariffPerKiloLitre());
        assertEquals(6.00, summary.getDailyCostInr()); // 400 L * 15 / 1000 = 6.00
        assertEquals(42.00, summary.getWeeklyCostInr()); // 2800 L * 15 / 1000 = 42.00
        assertEquals(180.00, summary.getMonthlyCostInr()); // 12000 L * 15 / 1000 = 180.00
        assertFalse(summary.getSlabTiers().isEmpty());
    }
}
