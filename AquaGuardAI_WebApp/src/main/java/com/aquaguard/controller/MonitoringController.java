package com.aquaguard.controller;

import com.aquaguard.service.DemoWaterDataService;
import com.aquaguard.service.WaterDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MonitoringController {

    private final WaterDataService dataService;

    public MonitoringController(WaterDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/live-monitoring")
    public String showLiveMonitoring(Model model) {
        model.addAttribute("activePage", "live-monitoring");
        model.addAttribute("systemStatus", dataService.getSystemStatus());
        model.addAttribute("readings", dataService.getLatestReadings());
        model.addAttribute("leakage", dataService.getLeakageEvaluation());
        model.addAttribute("settings", dataService.getSettings());

        if (dataService instanceof DemoWaterDataService demoService) {
            model.addAttribute("isSimulatingLeak", demoService.isSimulatingLeak());
        }

        return "live-monitoring";
    }
}
