package com.aquaguard.controller;

import com.aquaguard.model.MaintenanceRecord;
import com.aquaguard.service.WaterDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.UUID;

@Controller
public class MaintenanceController {

    private final WaterDataService dataService;

    public MaintenanceController(WaterDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/maintenance")
    public String showMaintenance(Model model) {
        model.addAttribute("activePage", "maintenance");
        model.addAttribute("systemStatus", dataService.getSystemStatus());
        model.addAttribute("maintenanceList", dataService.getMaintenanceRecords());
        model.addAttribute("newRecord", new MaintenanceRecord());
        return "maintenance";
    }

    @PostMapping("/maintenance/add")
    public String addMaintenanceRecord(@ModelAttribute MaintenanceRecord record,
                                       RedirectAttributes redirectAttributes) {
        if (record.getComponent() == null || record.getComponent().isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Component name is required.");
            return "redirect:/maintenance";
        }

        record.setId("MNT-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        if (record.getDate() == null || record.getDate().isBlank()) {
            record.setDate(LocalDate.now().toString());
        }
        if (record.getStatus() == null || record.getStatus().isBlank()) {
            record.setStatus("Completed");
        }

        dataService.addMaintenanceRecord(record);
        redirectAttributes.addFlashAttribute("successMessage", "New maintenance log entry recorded successfully.");
        return "redirect:/maintenance";
    }
}
