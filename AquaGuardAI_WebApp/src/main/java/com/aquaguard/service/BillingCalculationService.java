package com.aquaguard.service;

import com.aquaguard.model.BillingSummary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for water billing calculations and tariff estimation.
 */
@Service
public class BillingCalculationService {

    /**
     * Calculates the estimated water charge based on linear volumetric tariff.
     * Formula: (consumption in litres / 1000) * tariffPerKiloLitre
     */
    public double calculateLinearCost(double consumptionLitres, double tariffPerKiloLitre) {
        if (consumptionLitres <= 0 || tariffPerKiloLitre <= 0) {
            return 0.0;
        }
        double cost = (consumptionLitres / 1000.0) * tariffPerKiloLitre;
        return roundTwoDecimals(cost);
    }

    /**
     * Builds a comprehensive billing summary with linear and optional slab breakdown.
     */
    public BillingSummary generateBillingSummary(double dailyLitres, double weeklyLitres,
                                                 double monthlyLitres, double tariffPerKiloLitre) {
        BillingSummary summary = new BillingSummary();
        summary.setTariffPerKiloLitre(roundTwoDecimals(tariffPerKiloLitre));
        summary.setDailyConsumptionLitres(roundTwoDecimals(dailyLitres));
        summary.setDailyCostInr(calculateLinearCost(dailyLitres, tariffPerKiloLitre));

        summary.setWeeklyConsumptionLitres(roundTwoDecimals(weeklyLitres));
        summary.setWeeklyCostInr(calculateLinearCost(weeklyLitres, tariffPerKiloLitre));

        summary.setMonthlyConsumptionLitres(roundTwoDecimals(monthlyLitres));
        summary.setMonthlyCostInr(calculateLinearCost(monthlyLitres, tariffPerKiloLitre));

        // Projected next month (assuming 5% seasonal trend variance)
        double projectedMonthly = monthlyLitres * 1.05;
        summary.setProjectedNextMonthInr(calculateLinearCost(projectedMonthly, tariffPerKiloLitre));

        // Tiered Slab Demonstration:
        // Tier 1: 0 - 500 L @ Base Concessional Rate (₹8/kL)
        // Tier 2: 501 - 2,000 L @ Standard Rate (Configured Tariff)
        // Tier 3: > 2,000 L @ Conservation Surcharge Rate (Configured * 1.4)
        List<BillingSummary.SlabTier> slabList = new ArrayList<>();
        double remaining = monthlyLitres;
        double slabTotalCost = 0.0;

        // Tier 1
        double t1Volume = Math.min(remaining, 500.0);
        double t1Rate = 8.0;
        double t1Cost = (t1Volume / 1000.0) * t1Rate;
        slabList.add(new BillingSummary.SlabTier("Tier 1 (Base Lifeline)", "0 - 500 L", t1Rate, roundTwoDecimals(t1Volume), roundTwoDecimals(t1Cost)));
        remaining = Math.max(0, remaining - t1Volume);
        slabTotalCost += t1Cost;

        // Tier 2
        double t2Volume = Math.min(remaining, 1500.0);
        double t2Rate = tariffPerKiloLitre;
        double t2Cost = (t2Volume / 1000.0) * t2Rate;
        slabList.add(new BillingSummary.SlabTier("Tier 2 (Standard Usage)", "501 - 2,000 L", t2Rate, roundTwoDecimals(t2Volume), roundTwoDecimals(t2Cost)));
        remaining = Math.max(0, remaining - t2Volume);
        slabTotalCost += t2Cost;

        // Tier 3
        if (remaining > 0) {
            double t3Rate = roundTwoDecimals(tariffPerKiloLitre * 1.4);
            double t3Cost = (remaining / 1000.0) * t3Rate;
            slabList.add(new BillingSummary.SlabTier("Tier 3 (High Consumption Surcharge)", "> 2,000 L", t3Rate, roundTwoDecimals(remaining), roundTwoDecimals(t3Cost)));
            slabTotalCost += t3Cost;
        }

        summary.setSlabTiers(slabList);
        summary.setSlabEstimatedCostInr(roundTwoDecimals(slabTotalCost));

        return summary;
    }

    private double roundTwoDecimals(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
