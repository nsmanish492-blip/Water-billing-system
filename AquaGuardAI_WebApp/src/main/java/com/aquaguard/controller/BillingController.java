package com.aquaguard.controller;

import com.aquaguard.model.AppSettings;
import com.aquaguard.service.WaterDataService;
import com.aquaguard.service.DemoWaterDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BillingController {

    private final WaterDataService dataService;

    public BillingController(WaterDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/billing")
    public String showBilling(Model model) {
        model.addAttribute("activePage", "billing");
        model.addAttribute("systemStatus", dataService.getSystemStatus());
        model.addAttribute("billing", dataService.getBillingSummary());
        model.addAttribute("billingHistory", dataService instanceof DemoWaterDataService liveService
            ? liveService.getBillingHistory() : java.util.List.of());
        model.addAttribute("settings", dataService.getSettings());
        return "billing";
    }

    @PostMapping("/billing/update-tariff")
    public String updateTariff(@RequestParam("tariff") Double tariff, RedirectAttributes redirectAttributes) {
        if (tariff != null && tariff > 0) {
            AppSettings current = dataService.getSettings();
            current.setWaterTariffPerKiloLitre(tariff);
            dataService.updateSettings(current);
            redirectAttributes.addFlashAttribute("successMessage", "Water tariff updated to ₹" + tariff + " per 1,000 L.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid tariff rate provided.");
        }
        return "redirect:/billing";
    }
}
