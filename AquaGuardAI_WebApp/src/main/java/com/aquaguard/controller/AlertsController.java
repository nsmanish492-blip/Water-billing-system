package com.aquaguard.controller;

import com.aquaguard.service.WaterDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AlertsController {

    private final WaterDataService dataService;

    public AlertsController(WaterDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/alerts")
    public String showAlerts(Model model) {
        model.addAttribute("activePage", "alerts");
        model.addAttribute("systemStatus", dataService.getSystemStatus());
        model.addAttribute("alerts", dataService.getAlerts());
        return "alerts";
    }

    @PostMapping("/alerts/{id}/acknowledge")
    public String acknowledgeAlert(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        boolean success = dataService.acknowledgeAlert(id);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Alert " + id + " acknowledged successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Alert " + id + " not found.");
        }
        return "redirect:/alerts";
    }
}
