package com.aquaguard.controller;

import com.aquaguard.service.DemoWaterDataService;
import com.aquaguard.service.WaterDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LeakageController {

    private final WaterDataService dataService;

    public LeakageController(WaterDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/leakage-detection")
    public String showLeakageDetection(Model model) {
        model.addAttribute("activePage", "leakage-detection");
        model.addAttribute("systemStatus", dataService.getSystemStatus());
        model.addAttribute("readings", dataService.getLatestReadings());
        model.addAttribute("leakage", dataService.getLeakageEvaluation());
        model.addAttribute("settings", dataService.getSettings());

        if (dataService instanceof DemoWaterDataService demoService) {
            model.addAttribute("isSimulatingLeak", demoService.isSimulatingLeak());
        }

        return "leakage-detection";
    }

    @GetMapping("/future-risk")
    public String showFutureRisk(Model model) {
        model.addAttribute("activePage", "future-risk");
        model.addAttribute("systemStatus", dataService.getSystemStatus());
        model.addAttribute("futureRisk", dataService.getFutureRisk());
        model.addAttribute("pipeHealthList", dataService.getPipeHealthRecords());
        return "future-risk";
    }

    @GetMapping("/water-loss")
    public String showWaterLoss(Model model) {
        model.addAttribute("activePage", "water-loss");
        model.addAttribute("systemStatus", dataService.getSystemStatus());
        var records = dataService.getWaterLossRecords();
        model.addAttribute("waterLossList", records);
        model.addAttribute("totalEstimatedLossLitres", records.stream().mapToDouble(record -> record.getEstimatedLossLitres()).sum());
        model.addAttribute("totalEstimatedFinancialLoss", records.stream().mapToDouble(record -> record.getFinancialLossInr()).sum());
        double totalInletLitres = records.stream().mapToDouble(record -> record.getInletVolumeLitres()).sum();
        double totalLossLitres = records.stream().mapToDouble(record -> record.getEstimatedLossLitres()).sum();
        model.addAttribute("estimatedLossPercentage", totalInletLitres > 0 ? totalLossLitres / totalInletLitres * 100.0 : null);
        model.addAttribute("meanEstimatedLossRate", records.isEmpty() ? null : records.stream().mapToDouble(record -> record.getLossRateLpm()).average().orElse(0));
        model.addAttribute("settings", dataService.getSettings());
        return "water-loss";
    }

    @GetMapping("/pipe-health")
    public String showPipeHealth(Model model) {
        model.addAttribute("activePage", "pipe-health");
        model.addAttribute("systemStatus", dataService.getSystemStatus());
        model.addAttribute("pipeHealthList", dataService.getPipeHealthRecords());
        model.addAttribute("futureRisk", dataService.getFutureRisk());
        return "pipe-health";
    }
}
