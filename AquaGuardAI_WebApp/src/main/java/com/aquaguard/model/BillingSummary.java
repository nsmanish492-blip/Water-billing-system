package com.aquaguard.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Smart billing calculation model.
 */
public class BillingSummary {
    private double tariffPerKiloLitre; // INR per 1,000 L
    private double dailyConsumptionLitres;
    private double dailyCostInr;
    private double weeklyConsumptionLitres;
    private double weeklyCostInr;
    private double monthlyConsumptionLitres;
    private double monthlyCostInr;
    private double projectedNextMonthInr;
    private List<SlabTier> slabTiers = new ArrayList<>();
    private double slabEstimatedCostInr;
    private double currentConsumptionLitres;
    private boolean dailyAvailable;
    private boolean weeklyAvailable;
    private boolean monthlyAvailable;
    private String periodDataStatus = "INSUFFICIENT DATA";
    private String billingPeriodId;
    private String billingStatus = "CALCULATED ESTIMATE";

    public static class SlabTier {
        private String tierName;
        private String rangeLabel;
        private double ratePerKiloLitre;
        private double volumeInTierLitres;
        private double costInr;

        public SlabTier() {}

        public SlabTier(String tierName, String rangeLabel, double ratePerKiloLitre, double volumeInTierLitres, double costInr) {
            this.tierName = tierName;
            this.rangeLabel = rangeLabel;
            this.ratePerKiloLitre = ratePerKiloLitre;
            this.volumeInTierLitres = volumeInTierLitres;
            this.costInr = costInr;
        }

        public String getTierName() { return tierName; }
        public void setTierName(String tierName) { this.tierName = tierName; }
        public String getRangeLabel() { return rangeLabel; }
        public void setRangeLabel(String rangeLabel) { this.rangeLabel = rangeLabel; }
        public double getRatePerKiloLitre() { return ratePerKiloLitre; }
        public void setRatePerKiloLitre(double ratePerKiloLitre) { this.ratePerKiloLitre = ratePerKiloLitre; }
        public double getVolumeInTierLitres() { return volumeInTierLitres; }
        public void setVolumeInTierLitres(double volumeInTierLitres) { this.volumeInTierLitres = volumeInTierLitres; }
        public double getCostInr() { return costInr; }
        public void setCostInr(double costInr) { this.costInr = costInr; }
    }

    public BillingSummary() {}

    public double getTariffPerKiloLitre() { return tariffPerKiloLitre; }
    public void setTariffPerKiloLitre(double tariffPerKiloLitre) { this.tariffPerKiloLitre = tariffPerKiloLitre; }

    public double getDailyConsumptionLitres() { return dailyConsumptionLitres; }
    public void setDailyConsumptionLitres(double dailyConsumptionLitres) { this.dailyConsumptionLitres = dailyConsumptionLitres; }

    public double getDailyCostInr() { return dailyCostInr; }
    public void setDailyCostInr(double dailyCostInr) { this.dailyCostInr = dailyCostInr; }

    public double getWeeklyConsumptionLitres() { return weeklyConsumptionLitres; }
    public void setWeeklyConsumptionLitres(double weeklyConsumptionLitres) { this.weeklyConsumptionLitres = weeklyConsumptionLitres; }

    public double getWeeklyCostInr() { return weeklyCostInr; }
    public void setWeeklyCostInr(double weeklyCostInr) { this.weeklyCostInr = weeklyCostInr; }

    public double getMonthlyConsumptionLitres() { return monthlyConsumptionLitres; }
    public void setMonthlyConsumptionLitres(double monthlyConsumptionLitres) { this.monthlyConsumptionLitres = monthlyConsumptionLitres; }

    public double getMonthlyCostInr() { return monthlyCostInr; }
    public void setMonthlyCostInr(double monthlyCostInr) { this.monthlyCostInr = monthlyCostInr; }

    public double getProjectedNextMonthInr() { return projectedNextMonthInr; }
    public void setProjectedNextMonthInr(double projectedNextMonthInr) { this.projectedNextMonthInr = projectedNextMonthInr; }

    public List<SlabTier> getSlabTiers() { return slabTiers; }
    public void setSlabTiers(List<SlabTier> slabTiers) { this.slabTiers = slabTiers; }

    public double getSlabEstimatedCostInr() { return slabEstimatedCostInr; }
    public void setSlabEstimatedCostInr(double slabEstimatedCostInr) { this.slabEstimatedCostInr = slabEstimatedCostInr; }

    public double getCurrentConsumptionLitres() { return currentConsumptionLitres; }
    public void setCurrentConsumptionLitres(double currentConsumptionLitres) { this.currentConsumptionLitres = currentConsumptionLitres; }
    public boolean isDailyAvailable() { return dailyAvailable; }
    public void setDailyAvailable(boolean dailyAvailable) { this.dailyAvailable = dailyAvailable; }
    public boolean isWeeklyAvailable() { return weeklyAvailable; }
    public void setWeeklyAvailable(boolean weeklyAvailable) { this.weeklyAvailable = weeklyAvailable; }
    public boolean isMonthlyAvailable() { return monthlyAvailable; }
    public void setMonthlyAvailable(boolean monthlyAvailable) { this.monthlyAvailable = monthlyAvailable; }
    public String getPeriodDataStatus() { return periodDataStatus; }
    public void setPeriodDataStatus(String periodDataStatus) { this.periodDataStatus = periodDataStatus; }
    public String getBillingPeriodId() { return billingPeriodId; }
    public void setBillingPeriodId(String billingPeriodId) { this.billingPeriodId = billingPeriodId; }
    public String getBillingStatus() { return billingStatus; }
    public void setBillingStatus(String billingStatus) { this.billingStatus = billingStatus; }
}
