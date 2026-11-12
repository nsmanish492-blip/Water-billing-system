package com.aquaguard.controller;

import com.aquaguard.service.DemoWaterDataService;
import com.aquaguard.service.WaterDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final WaterDataService dataService;

    public DashboardController(WaterDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("systemStatus", dataService.getSystemStatus());
        model.addAttribute("readings", dataService.getLatestReadings());
        model.addAttribute("billing", dataService.getBillingSummary());
        model.addAttribute("leakage", dataService.getLeakageEvaluation());
        model.addAttribute("futureRisk", dataService.getFutureRisk());
        model.addAttribute("dailyUsage", dataService.getDailyUsage());
        model.addAttribute("pipeHealthList", dataService.getPipeHealthRecords());
        model.addAttribute("recentAlerts", dataService.getAlerts());
        model.addAttribute("waterLossList", dataService.getWaterLossRecords());
        model.addAttribute("settings", dataService.getSettings());

        if (dataService instanceof DemoWaterDataService demoService) {
            model.addAttribute("isSimulatingLeak", demoService.isSimulatingLeak());
        } else {
            model.addAttribute("isSimulatingLeak", false);
        }

        return "dashboard";
    }
}
