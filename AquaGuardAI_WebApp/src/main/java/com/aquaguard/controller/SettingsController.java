package com.aquaguard.controller;

import com.aquaguard.model.AppSettings;
import com.aquaguard.service.WaterDataService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SettingsController {

    private final WaterDataService dataService;

    public SettingsController(WaterDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/settings")
    public String showSettings(Model model) {
        model.addAttribute("activePage", "settings");
        model.addAttribute("systemStatus", dataService.getSystemStatus());
        if (!model.containsAttribute("settings")) {
            model.addAttribute("settings", dataService.getSettings());
        }
        return "settings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@Valid @ModelAttribute("settings") AppSettings settings,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "settings");
            model.addAttribute("systemStatus", dataService.getSystemStatus());
            model.addAttribute("errorMessage", "Validation errors encountered. Please correct the highlighted fields.");
            return "settings";
        }

        dataService.updateSettings(settings);
        redirectAttributes.addFlashAttribute("successMessage",
                "System configuration parameters successfully updated and active for this session.");
        return "redirect:/settings";
    }
}
