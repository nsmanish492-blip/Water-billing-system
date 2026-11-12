package com.aquaguard.controller;

import com.aquaguard.service.WaterDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DiagnosticsController {

    private final WaterDataService dataService;

    public DiagnosticsController(WaterDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/diagnostics")
    public String showDiagnostics(Model model) {
        model.addAttribute("activePage", "diagnostics");
        model.addAttribute("systemStatus", dataService.getSystemStatus());
        model.addAttribute("readings", dataService.getLatestReadings());
        model.addAttribute("leakage", dataService.getLeakageEvaluation());
        model.addAttribute("settings", dataService.getSettings());
        return "diagnostics";
    }
}
