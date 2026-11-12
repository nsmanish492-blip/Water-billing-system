package com.aquaguard.controller;

import com.aquaguard.service.WaterDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AnalyticsController {

    private final WaterDataService dataService;

    public AnalyticsController(WaterDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/analytics")
    public String showAnalytics(@RequestParam(value = "range", defaultValue = "24h") String range,
                                Model model) {
        model.addAttribute("activePage", "analytics");
        model.addAttribute("selectedRange", range);
        model.addAttribute("systemStatus", dataService.getSystemStatus());
        model.addAttribute("hourlyUsage", dataService.getHourlyUsage());
        model.addAttribute("dailyUsage", dataService.getDailyUsage());
        model.addAttribute("monthlyUsage", dataService.getMonthlyUsage());
        model.addAttribute("settings", dataService.getSettings());
        return "analytics";
    }
}
